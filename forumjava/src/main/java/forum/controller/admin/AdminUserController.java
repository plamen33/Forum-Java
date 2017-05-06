package forum.controller.admin;

import forum.entity.*;
import forum.repository.ReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import forum.bindingModel.UserEditBindingModel;
import forum.repository.TopicRepository;
import forum.repository.RoleRepository;
import forum.repository.UserRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ReplyRepository replyRepository;

    @GetMapping("/")
    public String listUsers(Model model){
        List<User> users = this.userRepository.findAll();

        model.addAttribute("users", users);

        return "admin/user/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }

        User user = this.userRepository.findOne(id);
        List<Role> roles = this.roleRepository.findAll();

        List<String> gender = new ArrayList<>();
        gender.add("Male");
        gender.add("Female");

        model.addAttribute("gender", gender);
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);


        return "admin/user/edit";
    }

    @PostMapping("/edit/{id}")
    public String editProcess(@PathVariable Integer id,
                              UserEditBindingModel userBindingModel, RedirectAttributes redirectAttributes){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }

        User user = this.userRepository.findOne(id);

        String newUserName= userBindingModel.getEmail();
        List<User> existingUsers = this.userRepository.findAll();
        existingUsers.remove(user);
        for (User u:existingUsers) {
            if(u.getEmail().toUpperCase().equals(newUserName.toUpperCase())){
                redirectAttributes.addFlashAttribute("error", "User already exists in Database - choose appropriate email");
                return "redirect:/admin/users/";
            }
        }


        if(!StringUtils.isEmpty(userBindingModel.getPassword())
                && !StringUtils.isEmpty(userBindingModel.getConfirmPassword())){

            if(userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())){
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

                user.setPassword(bCryptPasswordEncoder.encode(userBindingModel.getPassword()));
            }
        }

        user.setFullName(userBindingModel.getFullName());
        user.setEmail(userBindingModel.getEmail());
        user.setGender(userBindingModel.getGender());

        Set<Role> roles = new HashSet<>();

        for (Integer roleId : userBindingModel.getRoles()){
            roles.add(this.roleRepository.findOne(roleId));
        }

        user.setRoles(roles);

        this.userRepository.saveAndFlush(user);

        return "redirect:/admin/users/";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Model model){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }

        User user = this.userRepository.findOne(id);

        model.addAttribute("user", user);


        return "admin/user/delete";
    }

    @PostMapping("/delete/{id}")
    public String deleteProcess(@PathVariable Integer id,  RedirectAttributes redirectAttributes){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }
        User user = this.userRepository.findOne(id);

        /// checking if admin tries to delete himself:
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        User userEntity = this.userRepository.findByEmail(principal.getUsername());
        if(userEntity.getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "User cannot delete himself. Hacking the Java Forum Project is forbidden and will be persecuted !");
            return "redirect:/admin/users/";
        }

        for(Topic topic : user.getTopics()){
            // delete topic replies:
            List<Reply> replyList = this.replyRepository.findByTopic(topic);
            for (Reply reply: replyList) {
                this.replyRepository.delete(reply);
            }
            // delete topic
            this.topicRepository.delete(topic);
        }

        // delete user replies in topics where user is not the author
        for(Reply reply: user.getReplies()) {
            this.replyRepository.delete(reply);
        }

        //delete User pic:
        String root = System.getProperty("user.dir");
        String oldPic = user.getPicture();
        if (oldPic != null && !oldPic.equals("javauser.jpg")) {
            File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\users\\", oldPic);
            try {
                if (oldPicFile.delete()) {
                    System.out.println(oldPicFile.getName() + " is deleted!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Delete process failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ///////


        this.userRepository.delete(user);
        redirectAttributes.addFlashAttribute("deleted", "User successfully deleted !");
        return "redirect:/admin/users/";
    }
}
