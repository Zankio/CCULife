package org.zankio.cculife.CCUService.kiki.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.parser.BaseParser;
import org.zankio.cculife.CCUService.kiki.ScoreQuery;

public class ScoreQueryParser extends BaseParser {

    public String parserError(Document document) {
        StringBuilder message = new StringBuilder();
        Elements textNode;

        textNode = document.select("font");
        for (int i = 0; i < textNode.size(); i++) {
            message.append(textNode.get(i).text());
        }

        return message.toString();
    }

    public ScoreQuery.Grade[] parserGrade(Document document) {
        Elements grades, scores, fields;
        Element scoreTable;
        Node description;
        ScoreQuery.Grade[] result = null;

        grades = document.select("h3");
        result = new ScoreQuery.Grade[grades.size()];

        for (int i = 0; i < grades.size(); i++) {
            result[i] = new ScoreQuery.Grade();
            result[i].Grade = grades.get(i).text();

            scoreTable = grades.get(i).nextElementSibling();
            description = scoreTable.nextSibling();
            result[i].Description = description.outerHtml();

            if (scoreTable == null) continue;

            scores = scoreTable.select("tr");
            result[i].Scores = new ScoreQuery.Score[scores.size() - 1];

            for (int j = 1; j < scores.size(); j++) {
                fields = scores.get(j).select("td");

                result[i].Scores[j - 1] = new ScoreQuery.Score();
                result[i].Scores[j - 1].CoruseID = fields.get(0).text();
                result[i].Scores[j - 1].ClassID = fields.get(1).text();
                result[i].Scores[j - 1].CourseName = fields.get(2).text();
                result[i].Scores[j - 1].CreditType = fields.get(3).text();
                result[i].Scores[j - 1].Credit = fields.get(4).text();
                result[i].Scores[j - 1].Score = fields.get(5).text();

            }
        }

        return result;
    }
}
