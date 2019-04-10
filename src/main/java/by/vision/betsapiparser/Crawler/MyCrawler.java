package by.vision.betsapiparser.Crawler;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import by.vision.betsapiparser.*;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import javafx.scene.control.Hyperlink;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MyCrawler extends WebCrawler {

    TelegramBot telegramBot = new TelegramBot();

    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return href.startsWith("https://ru.betsapi.com/r/");
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String url = page.getWebURL().toString();
            Document doc = Jsoup.parse(htmlParseData.getHtml());

            CommonInfo commonInfo = new CommonInfo();
            getCommonInfo(commonInfo, doc, url);

            Elements infoInTr = doc.select("table.table-sm tr");
            //remove <span class="sr-only"> because of repetition parameters with <div ... role="progressbar"...>
            infoInTr.select("span.sr-only").remove();

            List<String[]> paramList = new ArrayList<>();

            for (Element trElement : infoInTr
            ) {
                Elements tdTagElements = trElement.select("td");
                int counter = 0;
                String[] paramInfoBlock = new String[3];
                for (Element elem : tdTagElements
                ) {
                    paramInfoBlock[counter] = elem.text();
                    counter++;
                    if (counter == 3) paramList.add(paramInfoBlock);
                }
            }

            TeamInfo leftMatch = new TeamInfo();
            TeamInfo rightMatch = new TeamInfo();

            for (String[] param : paramList
            ) {
                String header = param[1];
                String leftTeamParam = param[0];
                String rightTeamParam = param[2];

                switch (header) {
                    case "":
                        leftMatch.setClubName(param[0]);
                        rightMatch.setClubName(param[2]);
                        break;

                    case "Goals":
                        leftMatch.setGoals(Integer.parseInt(leftTeamParam));
                        rightMatch.setGoals(Integer.parseInt(rightTeamParam));
                        break;

                    case "Corners":
                        leftMatch.setCorners(Integer.parseInt(leftTeamParam));
                        rightMatch.setCorners(Integer.parseInt(rightTeamParam));
                        break;

                    case "Corners (Half)":
                        leftMatch.setCornersHalf(Integer.parseInt(leftTeamParam));
                        rightMatch.setCornersHalf(Integer.parseInt(rightTeamParam));
                        break;

                    case "Желтые карточки":
                        leftMatch.setCardYellow(Integer.parseInt(leftTeamParam));
                        rightMatch.setCardYellow(Integer.parseInt(rightTeamParam));
                        break;

                    case "Красные карточки":
                        leftMatch.setCardRed(Integer.parseInt(leftTeamParam));
                        rightMatch.setCardRed(Integer.parseInt(rightTeamParam));
                        break;

                    case "Пенальти":
                        leftMatch.setPenalties(Integer.parseInt(leftTeamParam));
                        rightMatch.setPenalties(Integer.parseInt(rightTeamParam));
                        break;

                    case "Замены":
                        leftMatch.setSubstitutions(Integer.parseInt(leftTeamParam));
                        rightMatch.setSubstitutions(Integer.parseInt(rightTeamParam));
                        break;

                    case "Атака":
                        leftMatch.setAttacks(Integer.parseInt(leftTeamParam));
                        rightMatch.setAttacks(Integer.parseInt(rightTeamParam));
                        break;

                    case "Опасная Атака":
                        leftMatch.setAttacksDangerous(Integer.parseInt(leftTeamParam));
                        rightMatch.setAttacksDangerous(Integer.parseInt(rightTeamParam));
                        break;

                    case "Удары в створ ворот":
                        leftMatch.setTargetOn(Integer.parseInt(leftTeamParam));
                        rightMatch.setTargetOn(Integer.parseInt(rightTeamParam));
                        break;

                    case "Удары мимо ворот":
                        leftMatch.setTargetOff(Integer.parseInt(leftTeamParam));
                        rightMatch.setTargetOff(Integer.parseInt(rightTeamParam));
                        break;

                    case "Владение мячом":
                        leftMatch.setPossession(Integer.parseInt(leftTeamParam));
                        rightMatch.setPossession(Integer.parseInt(rightTeamParam));
                        break;

                }
            }


            boolean possess = leftMatch.getPossession() >= Settings.possessionMin;
            boolean targetOn = leftMatch.getTargetOn() >= Settings.targetOnMin;
            boolean targetOff = leftMatch.getTargetOff() >= Settings.targetOffMin;
            boolean rightPossess = rightMatch.getPossession() >= Settings.possessionMin;
            boolean rightTargetOn = rightMatch.getTargetOn() >= Settings.targetOnMin;
            boolean rightTargetOff = rightMatch.getTargetOff() >= Settings.targetOffMin;

            switch (Settings.logic) {

                case OR:
                    if (possess || targetOn || targetOff) break;
                    if (rightPossess || rightTargetOn || rightTargetOff) break;
                    break;
                case AND:
                    if (possess && targetOn && targetOff) break;
                    if (rightPossess && rightTargetOn && rightTargetOff) break;
                    break;

            }

            //add link to GUI/Telegram
            notification(page, leftMatch, rightMatch, url);
        }

        logger.debug("=============");
    }

    public void getCommonInfo(CommonInfo commonInfo, Document doc, String url) {
        //time
        int time;
        Element timeElement = doc.selectFirst("div div h4 span[class=race-time]");
        time = Integer.parseInt(timeElement.ownText().replace("\'", ""));
        commonInfo.setTime(time);
        //do not add matches, that don't meet time value
        if(time < Settings.timeSelectMin || time > Settings.timeSelectMax ) return;

        //do not add matches, that already was sent to GUI/Telegram
        boolean inList = FXMLController.hyperlinkObservableList.stream()
                .anyMatch(hyperlink -> hyperlink.getText().endsWith(url));
        if (inList) return;

        //do not add matches, that don't meet score param
        Element scoreElement = doc.selectFirst("div div h4 span[class=text-danger]");
        commonInfo.setScore(scoreElement.ownText());

        MyLogger.ROOT_LOGGER.debug(url);

        //league
        commonInfo.setLeague(doc.selectFirst("nav ol li a").ownText());
        //matchId
        commonInfo.setIdMatch(doc.selectFirst("").ownText());

        //timesup
        Element timeSupElement = timeElement.selectFirst("sup");
        if (timeSupElement != null) {
            timeSupElement.remove();
        }

        //rate
        commonInfo.setRateL(doc.selectFirst(String.format("td[id=o_%s_0]", commonInfo.getIdMatch())).ownText());
        commonInfo.setRateC(doc.selectFirst(String.format("td[id=o_%s_1]", commonInfo.getIdMatch())).ownText());
        commonInfo.setRateR(doc.selectFirst(String.format("td[id=o_%s_2]", commonInfo.getIdMatch())).ownText());
        //clubs names
        commonInfo.setClubL(doc.selectFirst("td[class=text-right text-truncate] a").ownText());
        commonInfo.setClubR(doc.selectFirst("td[class=text-truncate] a").ownText());

        //mainPageInfoList.add(commonInfo);
    }

    private void notification(Page page, TeamInfo leftMatch, TeamInfo rightMatch, String url) {

        Hyperlink hyperlink = new Hyperlink(url);
        hyperlink.setOnAction(actionEvent -> {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(hyperlink.getText()));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
        FXMLController.hyperlinkObservableList.add(hyperlink);
        telegramBot.interestingMatch(url, leftMatch, rightMatch);
    }
}