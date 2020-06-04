/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nzoda
 */
@RestController
@RequestMapping("/api")
public class UserController {
    @RequestMapping(value = "user/{id}", method = RequestMethod.GET)
	public @ResponseBody Content getContent(@PathVariable("id") long id) {
		LogHelper.getLogger().logInfo("calling: /test/ " + id);

		Content content = new Content();
		content.setId(id);
		content.setData("paramiter: " + id);
		content.setDescription("That's works");
		content.setStatus(200);
		content.setError(null);

		return content;

	}
}
