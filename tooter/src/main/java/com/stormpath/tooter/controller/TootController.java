package com.stormpath.tooter.controller;

import com.stormpath.tooter.model.Customer;
import com.stormpath.tooter.model.Toot;
import com.stormpath.tooter.model.dao.CustomerDao;
import com.stormpath.tooter.model.dao.TootDao;
import com.stormpath.tooter.validator.TootValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ecrisostomo
 * Date: 6/8/12
 * Time: 6:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class TootController {

    TootValidator tootValidator;

    @Autowired
    TootDao tootDao;

    @Autowired
    CustomerDao customerDao;

    @Autowired
    public TootController(TootValidator tootValidator) {
        this.tootValidator = tootValidator;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tooter")
    public String processSubmit(@ModelAttribute("customer") Customer cust, BindingResult result, SessionStatus status) {

        tootValidator.validate(cust, result);

        if (result.hasErrors()) {
            //if validator failed
            //TODO: add SDK user validation
            return "tooter";
        } else {

            status.setComplete();

            //TODO: add Reset Password redirect logic. SDK?

            Toot tooot = new Toot();
            tooot.setTootMessage(cust.getTootMessage());
            tooot.setCustomer(cust);

            List<Toot> tootList = new ArrayList<Toot>();

            try {
                tootDao.saveToot(tooot);
                tootList = tootDao.getTootsByUserId(cust.getId());
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            cust.setTootMessage("");
            cust.setTootList(tootList);

            //form success
            return "tooter";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tooter")
    public String initForm(@RequestParam("accountId") String userName,
                           ModelMap model,
                           @ModelAttribute("customer") Customer cust,
                           BindingResult result) {

        List<Toot> tootList = new ArrayList<Toot>();

        Customer customer = new Customer();

        try {
            customer = customerDao.getCustomerByUserName(userName);
            tootList = tootDao.getTootsByUserId(customer.getId());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        model.addAttribute("tootList", tootList);

        model.addAttribute("customer", customer);

        //return form view
        return "tooter";
    }

    @RequestMapping("/tooter/remove")
    public String removeToot(@RequestParam("accountId") String userName,
                             @RequestParam("removeTootId") String removeTootId,
                             ModelMap model,
                             @ModelAttribute("customer") Customer cust) {


        Toot toot = new Toot();
        toot.setTootId(Integer.valueOf(removeTootId));

        try {
            tootDao.removeToot(toot);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        List<Toot> tootList = new ArrayList<Toot>();
        Customer customer = new Customer();

        try {
            customer = customerDao.getCustomerByUserName(userName);
            tootList = tootDao.getTootsByUserId(customer.getId());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        model.addAttribute("tootList", tootList);

        model.addAttribute("customer", customer);

        //return form view
        return "redirect:/tooter";
    }
}
