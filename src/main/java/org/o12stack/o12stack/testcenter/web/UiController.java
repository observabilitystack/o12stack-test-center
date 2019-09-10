package org.o12stack.o12stack.testcenter.web;

import org.o12stack.o12stack.testcenter.jobs.JobExecutor;
import org.o12stack.o12stack.testcenter.jobs.JobExecutor.PoolSize;
import org.o12stack.o12stack.testcenter.jobs.JobPublisher;
import org.o12stack.o12stack.testcenter.jobs.JobPublisher.Complexity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class UiController {

	@Autowired
	JobPublisher publisher;

	@Autowired
	JobExecutor executor;

	@GetMapping(path = "")
	public String index(Model model) {
		model.addAttribute("started", publisher.isRunning());
		
		model.addAttribute("poolSizes", PoolSize.values());
		model.addAttribute("currentPoolSize", executor.getPoolSize());
		
		model.addAttribute("complexities", Complexity.values());
		model.addAttribute("currentComplexity", publisher.getComplexity());
		
		model.addAttribute("outliers", publisher.isOutliers());
		
		return "index";
	}

	@GetMapping(path = "/jobs/publisher/start")
	public RedirectView start() {
		publisher.start();
		return new RedirectView("/");
	}

	@GetMapping(path = "/jobs/publisher/stop")
	public RedirectView stop() {
		publisher.stop();
		return new RedirectView("/");
	}

	@GetMapping(path = "/jobs/publisher/outliers/enable")
	public RedirectView enableOutliers() {
		publisher.setOutliers(true);
		return new RedirectView("/");
	}

	@GetMapping(path = "/jobs/publisher/outliers/disable")
	public RedirectView disableOutliers() {
		publisher.setOutliers(false);
		return new RedirectView("/");
	}

	@GetMapping(path = "/jobs/executor/poolsize/set")
	public RedirectView setSpeed(@RequestParam(required = true, name = "value") JobExecutor.PoolSize value) {
		executor.setPoolSize(value);
		return new RedirectView("/");
	}

	@GetMapping(path = "/jobs/publisher/complexity/set")
	public RedirectView setComplexity(@RequestParam(required = true, name = "value") JobPublisher.Complexity value) {
		publisher.setComplexity(value);
		return new RedirectView("/");
	}
	
	@GetMapping(path = "/jobs/publisher/drop-a-bomb")
	public RedirectView dropABomb() {
		executor.dropTheBomb();
		return new RedirectView("/");
	}

	

}
