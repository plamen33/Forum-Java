package forum.controller;

import forum.entity.Category;
import forum.entity.Forum;
import forum.entity.Topic;
import forum.repository.CategoryRepository;
import forum.repository.ForumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import forum.repository.TopicRepository;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ForumRepository forumRepository;

    @GetMapping("/")
    public String index(Model model) {


        List<Category> categories = this.categoryRepository.findAll();

        model.addAttribute("categories", categories);

        return "home/index";
    }
    @RequestMapping("/error/403")
    public String accessDenied(Model model){
        //model.addAttribute("view", "error/403");

        return "error/403";
    }
    @GetMapping("/category/{id}")
    public String listForums(Model model, @PathVariable Integer id){

        if(!this.categoryRepository.exists(id)){
            return "redirect:/";
        }

        Category category=this.categoryRepository.findOne(id);
        List<Forum> forums = this.forumRepository.findByCategory(category);

        forums.stream().sorted(Comparator.comparing(Forum::getId));


        model.addAttribute("forums", forums);
        model.addAttribute("category", category);

        return "home/list-forums";

    }

}
