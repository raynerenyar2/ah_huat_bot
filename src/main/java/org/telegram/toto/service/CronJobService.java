package org.telegram.toto.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.toto.bots.AhHuatBot;
import org.telegram.toto.models.Draw;
import org.telegram.toto.repository.ChatRepo;

@Service
public class CronJobService {

    @Autowired
    private WebscrapperService webscrapperService;
    @Autowired
    private AhHuatBot bot;
    @Autowired
    private ChatRepo chatRepo;
    @Autowired
    private SubscriberService subscriberService;

    private static final Logger logger = LoggerFactory.getLogger(CronJobService.class);

    // Monday and Thursday after 6:30 PM every 30 minutes until a draw is released
    @Scheduled(cron = "0 0/30 18/1 ? * MON,THU", zone = "Asia/Singapore")
    public void onTheDayRemind() {

        Optional<Draw> opt = webscrapperService.getNextDraw();
        opt.ifPresentOrElse(
                draw -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E,dMMMyyyy,h.mma", Locale.ENGLISH);
                    logger.info("Draw date time: "+draw.getDatetime().format(formatter) + " - Date time now: "+LocalDateTime.now().format(formatter));
                    boolean t = LocalDateTime.now().isBefore(draw.getDatetime());
                    logger.info("LocalDateTime.now().isBefore(draw.getDatetime() ==> " + t);
                    if (LocalDateTime.now().compareTo(draw.getDatetime()) < 0) {
                        List<String> chatIds = chatRepo.findAllByAlertValueNextDrawReceived(draw.getValue(), false);
                        if (!chatIds.isEmpty()) {
                            subscriberService.initialNotifySubLoud(chatIds, draw);
                            chatRepo.updateChatsNextDrawReceived(true, chatIds);
                        }
                    }
                },
                () -> {
                    List<String> chats = chatRepo.findAllReturnChatId();
                    chats.forEach(id -> {
                        bot.silent().send("Failed to get draw", Long.parseLong(id));
                    });
                    logger.error("Failed to get draw");
                });
//        if (opt.isPresent()) {
//            Draw draw = opt.get();
//            if (LocalDateTime.now().isBefore(draw.getDatetime())) {
//                List<String> chatIds = chatRepo.findAllByAlertValueNextDrawReceived(draw.getValue(), false);
//                if (!chatIds.isEmpty()) {
//                    subscriberService.initialNotifySubLoud(chatIds, draw);
//                    chatRepo.updateChatsNextDrawReceived(true, chatIds);
//                }
//            }
//        } else {
//            List<String> chats = chatRepo.findAllReturnChatId();
//            chats.forEach(id -> {
//                bot.silent().send("Failed to get draw", Long.parseLong(id));
//            });
//            logger.error("Failed to get draw");
//        }

    }

    // Resets nextDrawReceived to false
    @Scheduled(cron = "0 28 18 ? * MON,THU", zone = "Asia/Singapore")
    public void resetNextDrawReceived() {
        chatRepo.updateChatsNextDrawReceivedToFalse();
    }

    // repeat it everyday at 11am
    @Scheduled(cron = "0 0 11 * * ?", zone = "Asia/Singapore")
    public void dailyReminder() {

        Optional<Draw> opt = webscrapperService.getNextDraw();

        if (opt.isPresent()) {

            Draw draw = opt.get();
            List<String> chatIds = chatRepo.findAllByAlertValueNextDrawReceived(draw.getValue(), true);
            if (!chatIds.isEmpty()) {
                subscriberService.subsequentNotifySubLoud(chatIds, draw);
                // if (LocalDateTime.now().isAfter(draw.getDatetime())) {
                // chatRepo.updateChatsNextDrawReceived(false, chatIds);
                // }
            }

        } else {
            List<String> chats = chatRepo.findAllReturnChatId();
            chats.forEach(id -> {
                bot.silent().send("Failed to get draw", Long.parseLong(id));
            });
            logger.error("Failed to get draw");
        }
    }

}
