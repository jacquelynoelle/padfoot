package io.github.jacquelynoelle.padfoot.models;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.Calendar;

@IgnoreExtraProperties
public class Challenge {

    private String title;
    private int duration;
    private int goal;
    private Date startDate;
    private ChallengeStatus status;

    public Challenge() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Challenge(String title, int duration, int goal) {
        this.title = title;
        this.duration = duration;
        this.goal = goal;
    }

    public Challenge(String title, int duration, int goal, Date startDate, ChallengeStatus status) {
        this.title = title;
        this.duration = duration;
        this.goal = goal;
        this.startDate = startDate;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }

    public Date getEndDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DATE, duration);
        return cal.getTime();
    }
}
