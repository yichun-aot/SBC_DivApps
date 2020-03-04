package org.camunda.bpm.extension.hooks.task.listeners;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.*;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.extension.hooks.services.IEmail;
import org.camunda.bpm.extension.hooks.services.IMessageEvent;

/**
 * Timeout Task Listener to start a message event when the deadline is due
 *
 * @author yichun.zhao@aot-technologies.com
 */
public class TimeoutNotifyListener implements TaskListener, IEmail {

    private Expression remind;
    private Expression escalate;

    private static final Logger log = Logger.getLogger(TimeoutNotifyListener.class.getName());

    /**
     * This calculates time logic
     * and provides the necessary information to send message.
     *
     * @param delegateTask: The task which sends the message
     */
    public void notify(DelegateTask delegateTask) {

        if (delegateTask.getVariable("assigned_date") != null) {

            int remindTime = Integer.parseInt((String) this.remind.getValue(delegateTask));
            int escalateTime = Integer.parseInt((String) this.escalate.getValue(delegateTask));

            Date currentDate = new Date();
            Date assignedDate = null;
            try {
                assignedDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse((String) delegateTask.getVariable("assigned_date"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date remindDate = this.addMins(assignedDate, remindTime); // To be changed to addDays
            Date escalateDate = this.addMins(assignedDate, escalateTime); // To be changed to addDays
            Date stopNotifyDate = this.addMins(escalateDate, 1); // To be changed to addDays

            // Check if escalate first because reminder date is before escalation date
            if ((currentDate.after(escalateDate) && currentDate.before(stopNotifyDate))) {
                try {
                    sendEmail(delegateTask,"activity_escalation");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if ((currentDate.after(remindDate) && currentDate.before(stopNotifyDate))) {
                try {
                    sendEmail(delegateTask,"activity_reminder");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This adds days to a Date object.
     *
     * @param date: The date to be changed
     * @param days: The number of days to be added
     */
    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    // This is just for testing
    private Date addMins(Date date, int mins) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, mins);
        return calendar.getTime();
    }

}