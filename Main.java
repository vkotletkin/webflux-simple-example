package ru.kotletkin;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
// https://languagetool.org/download/ngram-data/untested/ngram-ru-20150914.zip
        JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("ru"));
//        langTool.activateLanguageModelRules(Path.of("src/main/resources/ngram-ru-20150914").toFile());
        // comment in to use statistical ngram data:
        //langTool.activateLanguageModelRules(new File("/data/google-ngram-data"));
        List<RuleMatch> matches = langTool.check("В нашом мире много проблем и сложностеи. Каждый день мы сталкемся с новыми вызовоми и препядствиями. Иногда кажеться что всё идёт не так как мы планировали.\n" +
                "Но не стоит отчаиватся! Главное верить в себя и не боятьса трудностей. Мы все делаем ошибки и это нормално. Важно учится на них и двигаться дальше.\n" +
                "Часто мы забываим о простых радостях жизни: о улыбке прохожего, о пении птиц утром, о тепле солнца на коже. Жизнь полна чудес если умееть их замечать.\n" +
                "Не прапускайте моменты счастья. Цените каждый день и людей которые рядом с вами. Они делают нашу жизнь ярче и насыщенне.\n" +
                "Помните: всё что нас не убивает — делает сильнее. И даже в самой тёмной ночи есть место для зари.");
        for (RuleMatch match : matches) {
            System.out.println("Potential error at characters " +
                    match.getFromPos() + "-" + match.getToPos() + ": " +
                    match.getMessage());
            System.out.println("Suggested correction(s): " +
                    match.getSuggestedReplacements());
        }

    }
}
