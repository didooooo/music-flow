package app.web.exceptionHandler;

import app.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String usernameAlreadyExist(UsernameAlreadyExistException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/register";
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public String emailAlreadyExist(EmailAlreadyExistException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/register";
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public String usernameNotFound(UsernameNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/login";
    }

    @ExceptionHandler(OrderDoesNotExistException.class)
    public ModelAndView orderDoesNotExist(OrderDoesNotExistException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.setViewName("error-page");
        return modelAndView;
    }

    @ExceptionHandler(PaymentMethodDeclinedException.class)
    public ModelAndView paymentMethodDeclined(PaymentMethodDeclinedException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.setViewName("error-page");
        return modelAndView;
    }

    @ExceptionHandler(StatisticsForTodayDoesNotExist.class)
    public ModelAndView statisticsDoesNotExist(StatisticsForTodayDoesNotExist ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.setViewName("error-page");
        return modelAndView;
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ModelAndView recordNotFound(RecordNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.setViewName("error-page");
        return modelAndView;
    }

    @ExceptionHandler(NotificationUpdateFailedException.class)
    public ModelAndView notificationUpdate(NotificationUpdateFailedException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.setViewName("error-page");
        return modelAndView;
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView defaultException(RuntimeException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.setViewName("error-page");
        return modelAndView;
    }



}
