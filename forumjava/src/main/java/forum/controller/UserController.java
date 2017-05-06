package forum.controller;


import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import forum.bindingModel.UserBindingModel;
import forum.entity.Role;
import forum.entity.User;
import forum.repository.RoleRepository;
import forum.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;


    @GetMapping("/register")
    public String register(Model model) {

        List<String> gender = new ArrayList<>();
        gender.add("Male");
        gender.add("Female");
        model.addAttribute("gender", gender);

        return "user/register";
    }

    @PostMapping("/register")
    public String registerProcess(UserBindingModel userBindingModel, RedirectAttributes redirectAttributes){

        if(!userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())){
            return "redirect:/register";
        }
        boolean userAlreadyExists = this.userRepository.findByEmail(userBindingModel.getEmail()) != null;

        if (userAlreadyExists){
            redirectAttributes.addFlashAttribute("error", "User already exists in Database !");
            return "redirect:/register";
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        LocalDateTime ldt = LocalDateTime.now();
        Timestamp regdate = Timestamp.valueOf(ldt);
        User user = new User(
                userBindingModel.getEmail(),
                userBindingModel.getFullName(),
                bCryptPasswordEncoder.encode(userBindingModel.getPassword()),
                userBindingModel.getGender(),
                regdate,
                userBindingModel.getPicture().getOriginalFilename()
        );

        Role userRole = this.roleRepository.findByName("ROLE_USER");

        user.addRole(userRole);
        // adding picture to Profile
        String root = System.getProperty("user.dir");
        MultipartFile file = userBindingModel.getPicture();

        if (file != null && (file.getSize() > 0 && file.getSize() < 77000)) {
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
            if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                /// add new picture
                String originalFileName = user.getFullName() + file.getOriginalFilename();
                File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\users\\", originalFileName);

                try {
                    file.transferTo(imageFile);
                    user.setPicture(originalFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            user.setPicture("javauser.jpg");
        }

        redirectAttributes.addFlashAttribute("success", "Successful registration.");


        this.userRepository.saveAndFlush(user);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model){
       // model.addAttribute("view", "user/login");

        return "user/login";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logoutPage (HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        redirectAttributes.addFlashAttribute("logout", "You have logged out !");
        return "redirect:/login?logout";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage(Model model){
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
       // model.addAttribute("view", "user/profile");

        return "user/profile";
    }
    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePost(UserBindingModel userBindingModel, MultipartFile file, RedirectAttributes redirectAttributes){
        try {
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            User user = this.userRepository.findByEmail(principal.getUsername());

            String root = System.getProperty("user.dir");
            file = userBindingModel.getPicture();

            if (file != null ) {
                if (file.getSize() > 0 && file.getSize() < 77000) {
                    String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
                    if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                        //delete old pic:

                        String oldPic = user.getPicture();
                        if (oldPic != null && !oldPic.equals("javauser.jpg")) {
                            File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\users\\", oldPic);
                            try {
                                if (oldPicFile.delete()) {
                                    System.out.println(oldPicFile.getName() + " is deleted!");
                                } else {
                                    redirectAttributes.addFlashAttribute("changePic", "Delete process failed !");
                                }
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("changePic", "Exception due to failure with delete file process !");
                                e.printStackTrace();
                            }
                        }
                        ///////

                        /// add new picture
                        String originalFileName = user.getFullName() + file.getOriginalFilename();
                        File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\users\\", originalFileName);

                        try {
                            file.transferTo(imageFile);
                            user.setPicture(originalFileName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } // image type limit
                    else{
                        redirectAttributes.addFlashAttribute("changePic", "You can upload only images !");
                    }
                } // size limit
                else{
                    redirectAttributes.addFlashAttribute("changePic", "The loaded file was skipped due to size restrictions - 77 kB limit size !");
                }
            }
            else {

            }

            this.userRepository.saveAndFlush(user);

            return "redirect:/profile";
        }// end of try
        catch(Exception e){
            e.printStackTrace();
            return "redirect:/profile";
        }
    }
}

