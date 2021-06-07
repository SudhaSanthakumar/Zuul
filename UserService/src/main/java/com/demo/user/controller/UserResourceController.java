package com.demo.user.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.demo.user.model.User;
import com.demo.user.model.Utility;
import com.demo.user.service.UserService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


@RestController
@RequestMapping("/v1")
public class UserResourceController {

	
	@Autowired
	private UserService userService;
	
	@Autowired
	RestTemplate restTemplate;
	
	Logger log=LoggerFactory.getLogger(UserResourceController.class);
	
	
	
	
	@GetMapping(value="/users/{userId}")
	public ResponseEntity<User> getuserById(@PathVariable ("userId") int userId,HttpServletRequest req) {
		
		
		log.info("User Service running in port "+req.getLocalPort());
		
		User user=userService.getUserById(userId).get();
		
		if(user==null) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		
		HttpHeaders headers=new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity httpHeaders=new HttpEntity<String>("parameters",headers);
		System.out.println("calling utility service");
		ResponseEntity<Utility[]> utilEntity=restTemplate.exchange("http://utility-service/v1/utilities",HttpMethod.GET,httpHeaders,Utility[].class);
		
		Set<Utility> utilSet=new HashSet<Utility>();
		for(Utility u: utilEntity.getBody())
			utilSet.add(u);
		user.setConsumedUtilitites(utilSet);
			
		
		return new ResponseEntity<User>(user,HttpStatus.OK);
	}
	
		
	@GetMapping(value="/users", produces=MediaType.APPLICATION_JSON_VALUE)
	@HystrixCommand(fallbackMethod="getAllUsersFallback")
	public ResponseEntity<?> getAllUsers(HttpServletRequest req) {
		
		log.info("User Service running in port "+req.getLocalPort());
		
		log.info("inside get users");
		
		HttpHeaders headers=new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity httpHeaders=new HttpEntity<String>("parameters",headers);
		System.out.println("calling utility service");
		ResponseEntity<Utility[]> utilEntity=restTemplate.exchange("http://utility-service/v1/utilities",HttpMethod.GET,httpHeaders,Utility[].class);
		
		for(Utility u:utilEntity.getBody())
			System.out.println(u.getUtilityId()+"  "+u.getUtilityName());
		
		return new ResponseEntity<List<User>>(userService.getUsers(), HttpStatus.FOUND);
	
	}
	
	@PostMapping(value="/users")
	public ResponseEntity<String> saveuser(@RequestBody User user ) {
		
		userService.save(user);
		return new ResponseEntity<String>(HttpStatus.CREATED);
		
	}
	
	
	
	@PutMapping(value="/users/{userId}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<User> updateuser(@RequestBody User user ,@PathVariable ("userId") int userId) {
		
		User s = userService.update(userId,user).get();
		HttpHeaders headers=new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<User>(s,headers,HttpStatus.ACCEPTED);
		
	}
	
	@DeleteMapping(value="/users/{userId}")
	public ResponseEntity<User> deleteuser(@PathVariable ("userId") int userId) {
		
		userService.delete(userId);
		
		return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
		
	}
	
	
	
    public ResponseEntity<?> getAllUsersFallback(HttpServletRequest req) {
		
		System.out.println("User Service running in port "+req.getLocalPort()+" and we are here in call back method");
		
		User user=new User(1,"Sudha","user","aaaa");
		List<User> users=new ArrayList<User>();
		users.add(user);
						
		return new ResponseEntity<String>("Cannot fetch data . please retry after some time", HttpStatus.FOUND);
	
	}
	
	
	

	@GetMapping(value="/users/search/{userName}")
	public ResponseEntity<User> getuserByUserName(@PathVariable ("userName") String userName) {
		
		log.info("inside getbyusername "+userName);
		User user=userService.getUserByUserName(userName);
		
		
		if(user==null) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<User>(user,HttpStatus.OK);
	}
	
	
	
	
}
