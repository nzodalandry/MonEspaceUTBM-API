package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.classic.Level;
import dtos.Content;
import dtos.PropertiesListData;
import exceptions.ApiInternalServerErrorHandlerException;
import exceptions.ApiMethodNotAllowedHandlerException;
import exceptions.ApiNotFoundHandlerException;
import helpers.LogHelper;

@RestController
@RequestMapping("/api")
public class CommonController extends BaseController{
	/**
	 * Get jboss.server.home.dir system property
	 * 
	 * @return value of system property jboss.server.home.dir
	 */
	@RequestMapping(value = "/jbossdir", method = RequestMethod.GET)
	public @ResponseBody Content getDirVar() {
		LogHelper.getLogger().logInfo("calling: /jbossdir");

		Content content = new Content();
		content.setId(System.currentTimeMillis());
		content.setData(System.getProperty("jboss.server.home.dir"));
		content.setDescription("get jboss.server.home.dir");
		content.setStatus(HttpStatus.OK.value());
		content.setError(null);

		return content;

	}

	/**
	 * Get list of log files
	 * 
	 * @param response
	 * @return
	 * @throws ApiNotFoundHandlerException, ApiInternalServerErrorHandlerException
	 */
	@RequestMapping(value = "/logfilesList", method = RequestMethod.GET)
	public @ResponseBody Content logfilesList(HttpServletRequest request, HttpServletResponse response)
			throws ApiNotFoundHandlerException, ApiInternalServerErrorHandlerException {
		LogHelper.getLogger().logInfo("calling: /logfilesList");
		String contentLog = System.getProperty("jboss.server.home.dir") + "/log";
		File pFile = new File(contentLog);
		List<String> fileLists = null;
		Content content = new Content();
		content.setId(System.currentTimeMillis());
		content.setDescription("get logs files");
		try {
			if (pFile.isDirectory() && pFile.exists()) {
				File[] fl = pFile.listFiles();
				if (fl != null && fl.length > 0) {
					fileLists = new ArrayList<>();

					for (File f : fl) {
						fileLists.add(f.getName());
					}

					content.setData(fileLists);
					content.setStatus(HttpStatus.OK.value());
					content.setError(null);

				} else {
					throw new ApiNotFoundHandlerException("Log directory is empty");
				}
			} else {
				throw new ApiNotFoundHandlerException("log directory not exists");
			}
		} catch (Exception e) {
			throw new ApiInternalServerErrorHandlerException(e.getMessage(), e);
		}
		return content;

	}

	/**
	 * Change log level
	 * 
	 * @param logLevel level to change debug,info,error,wern,off,trace
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ApiInternalServerErrorHandlerException
	 */
	@RequestMapping(value = "/logger/change", method = RequestMethod.POST)
	public @ResponseBody Content changeLogLevel(@RequestParam(value = "level", defaultValue = "info") String logLevel,
			HttpServletRequest request, HttpServletResponse response)
			throws ApiMethodNotAllowedHandlerException, ApiInternalServerErrorHandlerException {
		logger.logInfo("calling /logger/change/" + logLevel);
		Content content = new Content();
		content.setId(System.currentTimeMillis());
		content.setDescription("changing logger level");
		if (logLevel != null && !logLevel.isEmpty()) {
			Level lev = Level.INFO;
			boolean isLevelSet = false;
			if (logLevel.equalsIgnoreCase("info")) {
				lev = Level.INFO;
				isLevelSet = true;
			} else if (logLevel.equalsIgnoreCase("error")) {
				lev = Level.ERROR;
				isLevelSet = true;
			} else if (logLevel.equalsIgnoreCase("debug")) {
				lev = Level.DEBUG;
				isLevelSet = true;
			} else if (logLevel.equalsIgnoreCase("wern")) {
				lev = Level.WARN;
				isLevelSet = true;
			} else if (logLevel.equalsIgnoreCase("off")) {
				lev = Level.OFF;
				isLevelSet = true;
			} else if (logLevel.equalsIgnoreCase("trace")) {
				lev = Level.TRACE;
				isLevelSet = true;
			} else {
				throw new ApiMethodNotAllowedHandlerException("parameter level not acceppted");
			}
			if (isLevelSet) {
				logger.changeLogLevel(lev);
				content.setData(true);
				content.setStatus(HttpStatus.OK.value());
				content.setError(null);
			}
		} else {
			throw new ApiMethodNotAllowedHandlerException("missig parameter level");
		}
		return content;
	}

	/**
	 * crea il file di properties
	 * 
	 * @param propertiesPayload payload utilizzato come modello per la creazione del
	 *                          file di properties
	 * @param request
	 * @param response
	 * @return true se il file e' stato creato
	 * @throws ApiMethodNotAllowedHandlerException
	 * @throws ApiInternalServerErrorHandlerException
	 */
	@RequestMapping(value = "/properties/create", method = RequestMethod.PUT)
	@Description("crea il file di properties")
	public @ResponseBody Content createPropertiesFile(@RequestBody PropertiesListData propertiesPayload,
			HttpServletRequest request, HttpServletResponse response)
			throws ApiMethodNotAllowedHandlerException, ApiInternalServerErrorHandlerException {
		Content content = new Content();
		content.setDescription(
				"crea il file di properties per e lo pone nella directory path e lo nomina con filename");
		content.setError(null);
		content.setId(System.currentTimeMillis());
		content.setData(false);

		logger.logDebug("start create properties");

		if (propertiesPayload != null) {

			if (propertiesPayload.getFileName() != null && !propertiesPayload.getFileName().isEmpty()) {
				if (propertiesPayload.getPath() == null || propertiesPayload.getPath().isEmpty()) {
					propertiesPayload.setPath("");
				}

				// se il path termina per / o \ elimina
				if (!propertiesPayload.getPath().endsWith("/")) {
					propertiesPayload.setPath(propertiesPayload.getPath() + "/");
				}

				// crea tutta l'alberatura delle directory del path
				File pPathFile = new File(propertiesPayload.getPath());
				try {
					if (!pPathFile.exists()) {
						pPathFile.mkdirs();
					}

					String destinationPath = String
							.format("%s%s", propertiesPayload.getPath(), propertiesPayload.getFileName()).trim();

					// crea il testo del file
					File pOutFile = new File(destinationPath);

					// se esiste sovrascrivi
					// if(pOutFile.exists()){
					// pOutFile.delete();
					// }

					FileOutputStream outputFile = null;
					try {
						outputFile = new FileOutputStream(pOutFile);
						Map<String, String> varList = propertiesPayload.getProperties();
						if (varList != null && !varList.isEmpty()) {

							// aggiungi il commento se presente
							if (propertiesPayload.getFileComment() != null
									&& !propertiesPayload.getFileComment().isEmpty()) {
								outputFile.write((propertiesPayload.getFileComment().trim() + "\n\n").getBytes());
							}

							for (Map.Entry<String, String> entry : varList.entrySet()) {
								String bodyProperties = entry.getKey().trim() + "=" + entry.getValue().trim() + "\n";
								outputFile.write(bodyProperties.getBytes());
							}
						} else {
							outputFile.write("".getBytes());
						}
						content.setDescription("file write to: " + destinationPath);
						content.setData(true);
						content.setStatus(HttpStatus.OK.value());

					} catch (Exception e) {
						logger.logException(e);
						throw new ApiInternalServerErrorHandlerException(e.getMessage(), e);
					} finally {
						if (outputFile != null) {
							outputFile.close();
						}
					}
				} catch (Exception e) {
					logger.logException(e);
					throw new ApiInternalServerErrorHandlerException(e.getMessage(), e);
				}

			} else {
				logger.logDebug("create properties >> missing mandatory parameter filename");
				throw new ApiMethodNotAllowedHandlerException("missing parameter filename");
			}
		} else {
			logger.logDebug("create properties >> missing parameter body");
			throw new ApiMethodNotAllowedHandlerException("missing parameter body ");
		}
		return content;
	}

	@RequestMapping(value = "/properties/delete", method = RequestMethod.DELETE)
	@Description("elimina un file di properties")
	public @ResponseBody Content deletePropertiesFile(@RequestParam(value = "file", defaultValue = "") String file,
			HttpServletRequest request, HttpServletResponse response)
			throws ApiInternalServerErrorHandlerException, ApiMethodNotAllowedHandlerException {
		Content content = new Content();
		content.setDescription("Elimina il file di properties utilizzando il parametro file");
		content.setError(null);
		content.setId(System.currentTimeMillis());
		content.setData(false);

		if (file != null && !file.isEmpty()) {
			File pFile = new File(file);
			try {
				if (pFile.isFile() && pFile.exists()) {
					pFile.delete();
				}
				content.setStatus(HttpStatus.OK.value());
				content.setData(true);
			} catch (Exception e) {
				logger.logException(e);
				throw new ApiInternalServerErrorHandlerException(e.getMessage(), e);
			}
		} else {
			content.setError("missing paramiter");
			throw new ApiMethodNotAllowedHandlerException("missing parameter filename");
		}
		return content;
	}

	@RequestMapping(value = "/logger/readfile/{file}", method = RequestMethod.GET)
	@Description("legge un file del log")
	public @ResponseBody Content loggerReadFile(@PathVariable("file") String file, HttpServletRequest request,
			HttpServletResponse response)
			throws IOException, ApiInternalServerErrorHandlerException, ApiMethodNotAllowedHandlerException {
		Content content = new Content();
		content.setDescription("ottiene il contenuto di un file di log " + file);
		content.setError(null);
		content.setId(System.currentTimeMillis());
		content.setData(null);

		if (file != null && !file.isEmpty()) {
			if (!file.endsWith(".log")) {
				logger.logDebug("il file " + file + " non termina con il suffisso .log");
				file = String.format("%s.log", file);
			}

			String contentLog = System.getProperty("jboss.server.home.dir") + "/log/" + file;
			File pFile = new File(contentLog);

			if (pFile.isFile() && pFile.exists()) {
				// catica il contentuto
				Scanner in = null;
				try {
					in = new Scanner(pFile);
					String outputContent = "";
					while (in.hasNextLine()) {
						String line = in.nextLine();
						outputContent += line.trim() + "\n";
					}

					content.setData(outputContent);
					content.setStatus(HttpStatus.OK.value());

				} catch (Exception e) {
					logger.logException(e);
					throw new ApiInternalServerErrorHandlerException(e.getMessage(), e);
				} finally {
					if (in != null)
						in.close();
				}
			} else {
				logger.logError("the file " + contentLog + " not exists");
				FileNotFoundException fnEx = new FileNotFoundException("the file " + contentLog + " not exists");

				throw new ApiInternalServerErrorHandlerException(fnEx.getMessage(), fnEx);
			}
		} else {
			logger.logError("paramiter file is not set");
			throw new ApiMethodNotAllowedHandlerException("missing parameter file");
		}

		return content;
	}
}