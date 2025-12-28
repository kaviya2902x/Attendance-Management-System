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
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private RegularizationService regularizationService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // Today's attendance
            Attendance todayAttendance = attendanceService.getTodayAttendance(user.getId())
                    .orElse(null);

            // This month's attendance
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now().withDayOfMonth(
                    LocalDate.now().lengthOfMonth());
            List<Attendance> monthAttendance = attendanceService.getUserAttendance(
                    user.getId(), startOfMonth, endOfMonth);

            // Pending leaves
            List<Leave> pendingLeaves = leaveService.getUserLeaves(user.getId())
                    .stream()
                    .filter(l -> l.getStatus().equals("PENDING"))
                    .toList();

            // Recent attendance (last 7 days)
            LocalDate weekAgo = LocalDate.now().minusDays(7);
            List<Attendance> recentAttendance = attendanceService.getUserAttendance(
                    user.getId(), weekAgo, LocalDate.now());

            // Statistics
            long presentDays = monthAttendance.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()))
                    .count();
            long absentDays = monthAttendance.stream()
                    .filter(a -> "ABSENT".equals(a.getStatus()))
                    .count();
            double totalHours = monthAttendance.stream()
                    .filter(a -> a.getTotalHours() != null)
                    .mapToDouble(Attendance::getTotalHours)
                    .sum();

            model.addAttribute("user", user);
            model.addAttribute("todayAttendance", todayAttendance);
            model.addAttribute("monthAttendance", monthAttendance);
            model.addAttribute("pendingLeaves", pendingLeaves);
            model.addAttribute("recentAttendance", recentAttendance);
            model.addAttribute("presentDays", presentDays);
            model.addAttribute("absentDays", absentDays);
            model.addAttribute("totalHours", String.format("%.1f", totalHours));

        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
        }

        return "employee/dashboard";
    }

    @PostMapping("/punch-in")
    public String punchIn(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            Attendance attendance = attendanceService.punchIn(userId);
            redirectAttributes.addFlashAttribute("success",
                    "Punched in at " + attendance.getPunchIn().toLocalTime());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/dashboard";
    }

    @PostMapping("/punch-out")
    public String punchOut(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            Attendance attendance = attendanceService.punchOut(userId);
            redirectAttributes.addFlashAttribute("success",
                    "Punched out. Total hours: " +
                            String.format("%.1f", attendance.getTotalHours()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/dashboard";
    }

    @GetMapping("/attendance")
    public String attendance(HttpSession session, Model model,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        try {
            List<Attendance> attendanceList = attendanceService.getUserAttendance(
                    user.getId(), startDate, endDate);

            model.addAttribute("user", user);
            model.addAttribute("attendanceList", attendanceList);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading attendance: " + e.getMessage());
        }

        return "employee/attendance";
    }

    @GetMapping("/leave")
    public String leave(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            List<Leave> leaves = leaveService.getUserLeaves(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("leaves", leaves);
            model.addAttribute("leave", new Leave());

        } catch (Exception e) {
            model.addAttribute("error", "Error loading leaves: " + e.getMessage());
        }

        return "employee/leave";
    }

    @PostMapping("/leave/apply")
    public String applyLeave(@RequestParam String leaveType,
                             @RequestParam LocalDate startDate,
                             @RequestParam LocalDate endDate,
                             @RequestParam String reason,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            User user = userService.getUserById(userId).orElseThrow();

            Leave leave = new Leave();
            leave.setUser(user);
            leave.setLeaveType(leaveType);
            leave.setStartDate(startDate);
            leave.setEndDate(endDate);
            leave.setReason(reason);

            leaveService.applyLeave(leave);

            redirectAttributes.addFlashAttribute("success", "Leave applied successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/leave";
    }

    @GetMapping("/regularization")
    public String regularization(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            List<Regularization> regularizations = regularizationService
                    .getUserRegularizations(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("regularizations", regularizations);
            model.addAttribute("regularization", new Regularization());

        } catch (Exception e) {
            model.addAttribute("error", "Error loading regularizations: " + e.getMessage());
        }

        return "employee/regularization";
    }

    @PostMapping("/regularization/request")
    public String requestRegularization(@ModelAttribute Regularization regularization,
                                        @RequestParam String reason,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            User user = userService.getUserById(userId).orElseThrow();

            regularization.setUser(user);
            regularization.setReason(reason);
            regularizationService.requestRegularization(regularization);

            redirectAttributes.addFlashAttribute("success",
                    "Regularization requested successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/regularization";
    }

    @GetMapping("/onduty")
    public String onDuty(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "employee/onduty";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "employee/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User userDetails,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            User updatedUser = userService.updateUser(userId, userDetails);
            session.setAttribute("user", updatedUser);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/profile";
    }
}