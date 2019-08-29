package org.o12stack.o12stack.testcenter.web;

import java.util.Arrays;

import org.o12stack.o12stack.testcenter.jobs.JobPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class UiController {

	@Autowired
	JobPublisher publisher;

	@GetMapping(path = "")
	@ResponseBody
	public String index() {
		StringBuilder indexHtml = new StringBuilder();
		indexHtml.append("<h1>(ugly) temporary interface of O12Stack Test Center</h1>");
		indexHtml.append("<h3>... allows you to control submission of dummy Jobs into the system ...</h3>");
		indexHtml.append("Job Publisher is" + (publisher.isRunning() ? "" : " NOT") + " running.<br/>");
		if (publisher.isRunning()) {
			indexHtml.append("<a href=\"/jobs/publisher/stop\">STOP</a><br/>");
		} else {
			indexHtml.append("<a href=\"/jobs/publisher/start\">START</a><br/><br/>");
		}
		indexHtml.append("<br/>");
		indexHtml.append("<br/>");
		indexHtml.append("Job Publisher is" + (publisher.isOutliers() ? "" : " NOT") + " pushing outliers.<br/>");
		if (publisher.isOutliers()) {
			indexHtml.append("<a href=\"/jobs/publisher/outliers/disable\">DISABLE</a><br/><br/>");
		} else {
			indexHtml.append("<a href=\"/jobs/publisher/outliers/enable\">ENABLE</a><br/><br/>");
		}
		indexHtml.append("<br/>");
		indexHtml.append("<br/>");
		indexHtml.append("Job Publisher Speed is " + publisher.getSpeed() + ".<br/><br/>");
		Arrays.stream(JobPublisher.Speed.values())
				.map(speed -> "<a href=\"/jobs/publisher/speed/set?value=" + speed + "\">" + speed + "</a><br/>")
				.forEach(indexHtml::append);
		indexHtml.append("<br/>");
		indexHtml.append("Job Complexity is " + publisher.getComplexity() + ".<br/><br/>");
		Arrays.stream(JobPublisher.Speed.values()).map(complexity -> "<a href=\"/jobs/publisher/complexity/set?value="
				+ complexity + "\">" + complexity + "</a><br/>").forEach(indexHtml::append);
		indexHtml.append("<br/>");
		return indexHtml.toString();
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

	@GetMapping(path = "/jobs/publisher/speed/set")
	public RedirectView setSpeed(@RequestParam(required = true, name = "value") JobPublisher.Speed value) {
		publisher.setSpeed(value);
		return new RedirectView("/");
	}

	@GetMapping(path = "/jobs/publisher/complexity/set")
	public RedirectView setComplexity(@RequestParam(required = true, name = "value") JobPublisher.Complexity value) {
		publisher.setComplexity(value);
		return new RedirectView("/");
	}

}
