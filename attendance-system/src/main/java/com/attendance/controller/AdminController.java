package com.attendance.controller;

import com.attendance.entity.*;
import com.attendance.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private RegularizationService regularizationService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        try {
            // Statistics
            long totalUsers = userService.getTotalUsers();
            long totalEmployees = userService.getUsersByRole("EMPLOYEE").size();
            long pendingLeaves = leaveService.countPendingLeaves();
            long pendingRegularizations = regularizationService.countPendingRegularizations();

            // Recent users
            List<User> recentUsers = userService.getAllUsers().stream()
                    .limit(5)
                    .toList();

            // Recent leaves
            List<Leave> recentLeaves = leaveService.getPendingLeaves().stream()
                    .limit(5)
                    .toList();

            // Today's attendance
            List<Attendance> todayAttendance = attendanceService.getAllAttendance(LocalDate.now());
            long presentToday = todayAttendance.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()))
                    .count();
            long absentToday = todayAttendance.stream()
                    .filter(a -> "ABSENT".equals(a.getStatus()))
                    .count();

            // Active sessions
            List<Attendance> activeSessions = attendanceService.getActiveSessions();

            model.addAttribute("user", user);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalEmployees", totalEmployees);
            model.addAttribute("pendingLeaves", pendingLeaves);
            model.addAttribute("pendingRegularizations", pendingRegularizations);
            model.addAttribute("recentUsers", recentUsers);
            model.addAttribute("recentLeaves", recentLeaves);
            model.addAttribute("todayAttendance", todayAttendance);
            model.addAttribute("presentToday", presentToday);
            model.addAttribute("absentToday", absentToday);
            model.addAttribute("activeSessions", activeSessions);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
        }

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(HttpSession session, Model model,
                        @RequestParam(required = false) String search) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        try {
            List<User> users;
            if (search != null && !search.trim().isEmpty()) {
                users = userService.searchUsers(search);
            } else {
                users = userService.getAllUsers();
            }

            model.addAttribute("user", user);
            model.addAttribute("users", users);
            model.addAttribute("search", search);
            model.addAttribute("newUser", new User());

        } catch (Exception e) {
            model.addAttribute("error", "Error loading users: " + e.getMessage());
        }

        return "admin/users";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User newUser,
                          @RequestParam String confirmPassword,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        try {
            if (!newUser.getPassword().equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/admin/users";
            }

            if (userService.getUserByUsername(newUser.getUsername()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username already exists");
                return "redirect:/admin/users";
            }

            newUser.setDateOfJoining(LocalDate.now());
            userService.saveUser(newUser);

            redirectAttributes.addFlashAttribute("success", "User added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User userDetails,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, userDetails);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/attendance-management")
    public String attendanceManagement(HttpSession session, Model model,
                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        if (date == null) date = LocalDate.now();

        try {
            List<Attendance> attendanceList = attendanceService.getAllAttendance(date);

            model.addAttribute("user", user);
            model.addAttribute("attendanceList", attendanceList);
            model.addAttribute("selectedDate", date);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading attendance: " + e.getMessage());
        }

        return "admin/attendance-management";
    }

    @GetMapping("/leave-management")
    public String leaveManagement(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        try {
            List<Leave> pendingLeaves = leaveService.getPendingLeaves();
            List<Leave> allLeaves = leaveService.getAllLeaves();

            model.addAttribute("user", user);
            model.addAttribute("pendingLeaves", pendingLeaves);
            model.addAttribute("allLeaves", allLeaves);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading leaves: " + e.getMessage());
        }

        return "admin/leave-management";
    }

    @PostMapping("/leave/approve/{id}")
    public String approveLeave(@PathVariable Long id,
                               HttpSession session,
                               @RequestParam(required = false) String comments,
                               RedirectAttributes redirectAttributes) {
        try {
            User adminUser = (User) session.getAttribute("user");
            leaveService.approveLeave(id, adminUser.getUsername(), comments);
            redirectAttributes.addFlashAttribute("success", "Leave approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leave-management";
    }

    @PostMapping("/leave/reject/{id}")
    public String rejectLeave(@PathVariable Long id,
                              HttpSession session,
                              @RequestParam(required = false) String comments,
                              RedirectAttributes redirectAttributes) {
        try {
            User adminUser = (User) session.getAttribute("user");
            leaveService.rejectLeave(id, adminUser.getUsername(), comments);
            redirectAttributes.addFlashAttribute("success", "Leave rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leave-management";
    }

    @GetMapping("/regularization-management")
    public String regularizationManagement(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        try {
            List<Regularization> pendingRegularizations = regularizationService
                    .getPendingRegularizations();
            List<Regularization> allRegularizations = regularizationService
                    .getAllRegularizations();

            model.addAttribute("user", user);
            model.addAttribute("pendingRegularizations", pendingRegularizations);
            model.addAttribute("allRegularizations", allRegularizations);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading regularizations: " + e.getMessage());
        }

        return "admin/regularization-management";
    }

    @PostMapping("/regularization/approve/{id}")
    public String approveRegularization(@PathVariable Long id,
                                        HttpSession session,
                                        @RequestParam(required = false) String comments,
                                        RedirectAttributes redirectAttributes) {
        try {
            User adminUser = (User) session.getAttribute("user");
            regularizationService.approveRegularization(id, adminUser.getUsername(), comments);
            redirectAttributes.addFlashAttribute("success", "Regularization approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/regularization-management";
    }

    @PostMapping("/regularization/reject/{id}")
    public String rejectRegularization(@PathVariable Long id,
                                       HttpSession session,
                                       @RequestParam(required = false) String comments,
                                       RedirectAttributes redirectAttributes) {
        try {
            User adminUser = (User) session.getAttribute("user");
            regularizationService.rejectRegularization(id, adminUser.getUsername(), comments);
            redirectAttributes.addFlashAttribute("success", "Regularization rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/regularization-management";
    }

    @GetMapping("/reports")
    public String reports(HttpSession session, Model model,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        try {
            List<Attendance> attendanceList = attendanceService.getAttendanceByDateRange(startDate, endDate);
            List<User> allUsers = userService.getAllUsers();
            List<Leave> leaves = leaveService.getAllLeaves();

            long presentCount = attendanceList.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()))
                    .count();
            long absentCount = attendanceList.stream()
                    .filter(a -> "ABSENT".equals(a.getStatus()))
                    .count();

            model.addAttribute("user", user);
            model.addAttribute("attendanceList", attendanceList);
            model.addAttribute("allUsers", allUsers);
            model.addAttribute("leaves", leaves);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("presentCount", presentCount);
            model.addAttribute("absentCount", absentCount);

        } catch (Exception e) {
            model.addAttribute("error", "Error generating reports: " + e.getMessage());
        }

        return "admin/reports";
    }
}