package forum.controller.admin;

import forum.entity.*;
import forum.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import forum.bindingModel.CategoryBindingModel;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ForumRepository forumRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private ReplyRepository replyRepository;

    @GetMapping("/")
    public String list(Model model){

        List<Category> categories = this.categoryRepository.findAll();
        categories = categories.stream()
                .sorted(Comparator.comparingInt(Category::getId))
                .collect(Collectors.toList());
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("categories", categories);
        return "admin/category/list";
    }

    @GetMapping("/create")
    public String create (Model model){
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        return "admin/category/create";
    }

    @PostMapping("/create")
    public String createProcess(CategoryBindingModel categoryBindingModel, RedirectAttributes redirectAttributes, MultipartFile file){

        if(StringUtils.isEmpty(categoryBindingModel.getName())){
            redirectAttributes.addFlashAttribute("error", "Category should not be empty");
            return "redirect:/admin/categories/create";
        }
        //// checking if the create category name already exists
        boolean categoryAlreadyExists = this.categoryRepository.findByName(categoryBindingModel.getName())!=null;
        if(categoryAlreadyExists){
            redirectAttributes.addFlashAttribute("error", "Category already exists in Database - choose appropriate name");
            return "redirect:/admin/categories/create";
        }

        Category category = new Category(categoryBindingModel.getName());

        /// adding category Image
        String root = System.getProperty("user.dir");
        file = categoryBindingModel.getPicture();

        if (file != null ) {
            if (file.getSize() > 0 && file.getSize() < 77000) {
                String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
                if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                    /// add new picture
                    String originalFileName = category.getName()+ "-" + file.getOriginalFilename();
                    File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\categories\\", originalFileName);

                    try {
                        file.transferTo(imageFile);
                        category.setPicture(originalFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } // image type limit
                else{
                    redirectAttributes.addFlashAttribute("error", "Your file was skipped, you can upload only images (jpg, png and gif) !");
                }
            } // size limit
            else{
                redirectAttributes.addFlashAttribute("error", "Your file was skipped, due to size restrictions - 77Kb max !");
            }
        }
        else {
            redirectAttributes.addFlashAttribute("error", "Invalid file.");
        }

        this.categoryRepository.saveAndFlush(category);
        return "redirect:/admin/categories/";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Integer id){
        if(!this.categoryRepository.exists(id)){
            return "redirect:/admin/categories/";
        }
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        Category category = this.categoryRepository.findOne(id);
        model.addAttribute("category", category);
        return "admin/category/edit";
    }

    @PostMapping("/edit/{id}")
    public String editProcess(@PathVariable Integer id, CategoryBindingModel categoryBindingModel, MultipartFile file, RedirectAttributes redirectAttributes){

        if(!this.categoryRepository.exists(id)){
            return "redirect:/admin/categories/";
        }

        Category category=this.categoryRepository.findOne(id);
        //// checking if the edit name already exists
        String newCategoryName= categoryBindingModel.getName();
        List<Category> existingCategories = this.categoryRepository.findAll();
        existingCategories.remove(category);
        for (Category c:existingCategories) {
            if(c.getName().toUpperCase().equals(newCategoryName.toUpperCase())){
                redirectAttributes.addFlashAttribute("error", "Category already exists in Database - choose appropriate name");
                return "redirect:/admin/categories/edit/{id}";
            }
        }

        /// changing category Image
        String root = System.getProperty("user.dir");
        file = categoryBindingModel.getPicture();

        if (file != null ) {
            if (file.getSize() > 0 && file.getSize() < 77000) {
                String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
                if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                    //delete old pic:
                    String oldPic = category.getPicture();
                    System.out.println(oldPic);
                    if (oldPic != null) {
                        File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\categories\\", oldPic);
                        try {
                            if (oldPicFile.delete()) {
                                System.out.println(oldPicFile.getName() + " is deleted!");
                            } else {
                                System.out.println("Delete operation failed.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ///////

                    /// add new picture
                    String originalFileName = category.getName()+ "-" + file.getOriginalFilename();
                    File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\categories\\", originalFileName);

                    try {
                        file.transferTo(imageFile);
                        category.setPicture(originalFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } // image type limit
                else{
                    redirectAttributes.addFlashAttribute("error", "Your file was skipped, you can upload only images (jpg, png and gif) !");
                }
            } // size limit
            else{
                redirectAttributes.addFlashAttribute("error", "Your file was skipped, due to size restrictions - 77Kb max image size !");
            }
        }
        else {

        }

        category.setName(categoryBindingModel.getName());
        this.categoryRepository.saveAndFlush(category);
        return "redirect:/admin/categories/";
    }

    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Integer id){
        if(!this.categoryRepository.exists(id)){
            return "redirect:/admin/categories/";
        }
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        Category category = this.categoryRepository.findOne(id);
        model.addAttribute("category", category);
        return "admin/category/delete";
    }

    @PostMapping("/delete/{id}")
    public String deleteProcess(@PathVariable Integer id){
        if(!this.categoryRepository.exists(id)){
            return "redirect:/admin/categories/";
        }
        Category category = this.categoryRepository.findOne(id);

        for (Forum forum:category.getForums()){

            for (Topic topic: forum.getTopics()){
                // delete topic replies:
                List<Reply> replyList = this.replyRepository.findByTopic(topic);
                for (Reply reply: replyList) {
                    this.replyRepository.delete(reply);
                }
                // delete topic itself
                this.topicRepository.delete(topic);
            }

            this.forumRepository.delete(forum);
        }

        //delete category pic:
        String root = System.getProperty("user.dir");
        String oldPic = category.getPicture();
        System.out.println(oldPic);
        if (oldPic != null) {
            File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\categories\\", oldPic);
            try {
                if (oldPicFile.delete()) {
                    System.out.println(oldPicFile.getName() + " is deleted!");
                } else {
                    System.out.println("Delete operation failed.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ///////
        this.categoryRepository.delete(category);
        return "redirect:/admin/categories/";
    }
}
