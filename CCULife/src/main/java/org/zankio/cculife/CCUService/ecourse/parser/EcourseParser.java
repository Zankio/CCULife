package org.zankio.cculife.CCUService.ecourse.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.parser.BaseParser;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Homework;
import org.zankio.cculife.override.Exceptions;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class EcourseParser extends BaseParser {

    public Elements parseRow(Elements elements) {
        return elements.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
    }

    public Elements parseRow(Element elements) {
        return elements.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
    }

    public Elements parseField(Element elements) {
        return elements.select("td");
    }

    public Elements parseField(Elements elements) {
        return elements.select("td");
    }

    public Ecourse.Course[] parserCourses(Ecourse ecourse, Document document) {
        Elements tables, courses = null, fields;
        Ecourse.Course[] result = null;
        tables = document.select("table");

        for (Element table : tables) {
            courses = table.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
            if (courses.size() > 0) break;
        }
        if (courses == null) return null;
        result = new Ecourse.Course[courses.size()];

        for (int i = 0; i < courses.size(); i++) {
            fields = courses.get(i).getElementsByTag("td");
            result[i] = ecourse.new Course(ecourse);

            result[i].courseid = fields.get(3).child(0).child(0).attr("href").replace("../login_s.php?courseid=", "");
            result[i].id = fields.get(2).text();
            result[i].name = fields.get(3).text();
            result[i].teacher = fields.get(4).text();
            result[i].notice = Integer.parseInt(fields.get(5).text());
            result[i].homework = Integer.parseInt(fields.get(6).text());
            result[i].exam = Integer.parseInt(fields.get(7).text());
            result[i].warning = !fields.get(9).text().equals("--");

        }

        return result;
    }

    public Ecourse.Scores[] parserScore(Ecourse ecourse, Document document) {
        Elements scores, fields;
        ArrayList<Ecourse.Scores> result;
        ArrayList<Ecourse.Score> score = null;
        Ecourse.Scores mScores = null;
        Ecourse.Score mScore = null;

        scores = document.select("tr[bgcolor=#4d6eb2], tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");

        result = new ArrayList<Ecourse.Scores>();

        for(int i = 0; i < scores.size(); i++) {
            fields = scores.get(i).select("td");

            // Name row check
            if ("#4d6eb2".equals(scores.get(i).attr("bgcolor"))) {
                if (mScores != null && score.size() > 0) {
                    mScores.scores = score.toArray(new Ecourse.Score[score.size()]);
                    result.add(mScores);
                }

                mScores = ecourse.new Scores();
                mScores.Name = fields.get(0).text().replace("(名稱)", "");
                score = new ArrayList<Ecourse.Score>();
                continue;
            }

            mScore = ecourse.new Score();
            mScore.Name = fields.get(0).text();
            mScore.Percent = fields.get(1).text();
            mScore.Score = fields.get(2).text();
            mScore.Rank = fields.get(3).text();

            assert score != null;
            score.add(mScore);
        }

        assert score != null;
        if (score.size() > 0) {
            mScores.scores = score.toArray(new Ecourse.Score[score.size()]);
            result.add(mScores);
        }

        mScores = ecourse.new Scores();
        mScores.Name = "總分";
        scores = document.select("tr[bgcolor=#B0BFC3]");
        if (scores.size() >= 2) {
            fields = scores.get(0).select("th");
            if (fields.size() >= 2) mScores.Rank = fields.get(1).text();
            fields = scores.get(1).select("th");
            if (fields.size() >= 2) mScores.Score = fields.get(1).text();
        }

        if (mScores.Rank != null && !"你沒有成績".equals(mScores.Rank)) result.add(mScores);


        return result.toArray(new Ecourse.Scores[result.size()]);
    }

    public Ecourse.Classmate[] parserClassmate(Ecourse ecourse, Document document) {
        Elements list, field;

        Ecourse.Classmate[] result;

        list = document.select("tr[bgcolor=#F0FFEE], tr[bgcolor=#E6FFFC]");

        result = new Ecourse.Classmate[list.size()];

        for (int i = 0; i < list.size(); i++) {
            field = list.get(i).select("td");

            result[i] = ecourse.new Classmate();
            result[i].Name = field.get(3).text();
            result[i].Department = field.get(1).text();
            result[i].Gender = field.get(5).text();
            result[i].StudentId = field.get(2).text();

        }

        return result;
    }

    public Ecourse.Announce[] parserAnnounce(Ecourse.Course course, Document document) {
        Elements announces, fields;
        Ecourse ecourse = course.getEcourse();
        Ecourse.Announce[] result;

        announces = document.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
        result = new Ecourse.Announce[announces.size()];

        for(int i = 0; i < result.length; i++) {
            fields = announces.get(i).select("td");

            result[i] = ecourse.new Announce(ecourse, course);
            result[i].Date =  fields.get(0).text();
            result[i].Title = fields.get(2).text();
            result[i].important = fields.get(1).text();
            result[i].browseCount = Integer.parseInt(fields.get(3).text());
            result[i].isnew = fields.get(2).select("img").size() > 0;
            result[i].url = fields.get(2).child(0).child(0).child(0).attr("onclick").split("'")[1].replace("./", "");
        }

        return result;
    }

    public Homework[] parseHomework(Document document, Ecourse ecourse, Ecourse.Course course) {
        Elements row, field;
        Homework[] result;

        row = parseRow(document);
        result = new Homework[row.size()];

        for (int i = 0; i < result.length; ++i) {
            field = parseField(row.get(i));
            result[i] = new Homework(ecourse, course);
            result[i].id = Integer.valueOf(field.get(5).child(1).val());
            result[i].title = field.get(1).text();
            result[i].deadline = field.get(3).text();
            result[i].score = field.get(4).text();
        }

        return result;
    }

    public void parserFilesListFiles(ArrayList<Ecourse.File> filelist, String baseurl, Ecourse ecourse, Document document) {
        Elements files;
        Element nodeFile, nodeSize;
        Ecourse.File file;

        String nodeHref;
        boolean standFileTemplate = false;

        if (baseurl.startsWith("http://ecourse.ccu.edu.tw/php/textbook/course_menu.php")) standFileTemplate = true;
        if (baseurl.startsWith("https://ecourse.ccu.edu.tw/php/textbook/course_menu.php")) standFileTemplate = true;

        files = document.select("a");

        for (int i = 0; i < files.size(); i++) {
            nodeFile = files.get(i);
            nodeHref = nodeFile.attr("href");

            if(nodeHref == null || nodeHref.equals("FILE_LINK") || nodeHref.startsWith("mailto:")) continue;

            nodeHref = setBaseUrl(nodeHref, baseurl);

            if (Pattern.matches("^https?\\:\\/\\/ecourse(?:\\.elearning)?\\.ccu\\.edu\\.tw\\/[^/]+\\/textbook\\/.+$", nodeHref)) {

                file = ecourse.new File();
                file.Name = getFileName(nodeHref);
                file.URL = nodeHref;

                if (standFileTemplate) {
                    nodeSize = nodeFile.parent().nextElementSibling();
                    file.Name = nodeFile.text();
                    file.Size = nodeSize.text();
                }

                filelist.add(file);
            }
        }
    }


    private String getFileName(String url) {
        url = url.substring(url.lastIndexOf('/') + 1);
        try {
            url = java.net.URLDecoder.decode(url, "ISO-8859-1");
            return new String(url.getBytes("ISO-8859-1"), "big5");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    private String setBaseUrl(String url, String base) {
        if(url == null ||
                url.startsWith("http://") ||
                url.startsWith("https://") ||
                url.startsWith("ftp://") ||
                url.startsWith("mailto:")
                ) return url;

        URL mUrl;
        try {
            mUrl = new URL(base);
            return mUrl.getProtocol() + "://" + mUrl.getHost() + mUrl.getFile().substring(0, mUrl.getFile().lastIndexOf('/') + 1) + url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public String parserAnnounceContent(Document document) throws Exception{
        Elements rows;

        rows = document.select("td[bgcolor=#E8E8E8]");

        if (rows.size() > 2 )
            return rows.get(2).html();
        else
            throw new Exception("讀取資料錯誤");

    }

    public void parserHomeworkContent(Document document, Homework homework) throws Exception {
        Elements content;

        content = document.select("pre");
        if (content.size() == 0) throw new Exception("讀取作業題目錯誤");

        homework.content = content.html();
    }
}
