import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;

/**
 * @author AleksandrovichK
 */
public class Main {
    public static void main(String[] args) {
        var cluster = new Parser().parseJavaClasses();
        var paths = new Parser().parseRestPaths("/RestApiEndpoints.java");

        for (RestClass rest : cluster) {
            createDoc(rest, paths);
        }
    }

    private static void createDoc(RestClass restClass, Map<String, String> paths) {
        try {
            XWPFDocument docxModel = new XWPFDocument();

            var services = restClass.getServices();
            toHandleRestPaths(services, paths);
            toSortServicesByLength(services);
            toSetDocHeader(docxModel);

            XWPFTable tableX = docxModel.createTable(services.size() + 1, 3);
            toSetTableBorders(tableX);
            toSetTableHeader(tableX);

            for (int i = 0; i < services.size(); i++) {
                XWPFTableRow row2 = tableX.getRow(i + 1);
                tableCell(row2, 0, services.get(i).type);
                tableCell(row2, 1, services.get(i).url);
                tableCell(row2, 2, services.get(i).description);
            }

            FileOutputStream outputStream = new FileOutputStream("D:/WorkAuxiliary/JAD/GENERATED DOC/" + restClass.getClassName() + ".docx");
            docxModel.write(outputStream);
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Записан: " + restClass.getClassName() + ".docx");
    }

    private static void toSetDocHeader(XWPFDocument docxModel) {
        XWPFParagraph tableHeader = docxModel.createParagraph();
        tableHeader.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun tableHeaderRun = tableHeader.createRun();
        tableHeaderRun.setFontSize(20);
        tableHeaderRun.setFontFamily("Microsoft JhengHei UI Light");
        tableHeaderRun.setColor("1F4E79");
        tableHeaderRun.setText("Сервисы <AUTOGENERATED>");
    }

    private static void toSetTableHeader(XWPFTable tableX) {
        XWPFTableRow header = tableX.getRow(0);
        headerCell(header, 0, "1250", "Тип");
        headerCell(header, 1, "4000", "URL");
        headerCell(header, 2, "4000", "Описание");
    }

    private static void toSetTableBorders(XWPFTable tableX) {
        CTTblPr tbl = tableX.getCTTbl().getTblPr();

        CTTblBorders borders = tbl.addNewTblBorders();
        borders.addNewBottom().setVal(STBorder.NONE);
        borders.addNewLeft().setVal(STBorder.NONE);
        borders.addNewRight().setVal(STBorder.NONE);
        borders.addNewTop().setVal(STBorder.NONE);
        borders.addNewInsideH().setVal(STBorder.NONE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);
    }

    private static void toSortServicesByLength(List<Service> services) {
        Collections.sort(services, Comparator.comparingInt(o -> o.url.length()));
    }

    private static void toHandleRestPaths(List<Service> services, Map<String, String> paths) {
        for (Map.Entry<String, String> entry : paths.entrySet()) {
            for (Service service : services) {
                if (service.url.contains(entry.getKey())) {
                    service.url = service.url.replace(entry.getKey(), entry.getValue());
                }
            }
        }

        for (Service service : services) {
            if (service.url.contains(" + ")) {
                service.url = service.url.replace(" + ", "");
            }
        }
    }

    private static void headerCell(XWPFTableRow header, int position, String width, String text) {
        header.getCell(position).setColor("BFBFBF");
        header.getCell(position).setWidth(width);

        XWPFParagraph par = header.getCell(position).addParagraph();
        par.setAlignment(ParagraphAlignment.CENTER);
        par.setVerticalAlignment(TextAlignment.CENTER);

        XWPFRun run = par.createRun();
        run.setColor("000000");
        run.setFontFamily("Microsoft JhengHei UI Light");
        run.setText(text);
        run.setBold(true);
    }

    private static void tableCell(XWPFTableRow row, int position, String text) {
        XWPFParagraph par = row.getCell(position).addParagraph();
        XWPFRun run = par.createRun();
        run.setFontFamily("Microsoft JhengHei UI Light");
        run.setFontSize(10);

        switch (position) {
            case 0: {

                switch (text) {
                    case "@GET": {
                        row.getCell(position).setColor("DAFAF9");
                        run.setText("GET");
                        break;
                    }
                    case "@POST": {
                        row.getCell(position).setColor("FFF7DD");
                        run.setText("POST");
                        break;
                    }
                    case "@PUT": {
                        row.getCell(position).setColor("DAE8FE");
                        run.setText("PUT");
                        break;
                    }
                    case "@DELETE": {
                        row.getCell(position).setColor("FFE7E8");
                        run.setText("DELETE");
                        break;
                    }
                }
                par.setAlignment(ParagraphAlignment.CENTER);
                par.setVerticalAlignment(TextAlignment.CENTER);
                break;
            }
            case 1: {
                row.getCell(position).setColor("FFFFFF");
                par.setAlignment(ParagraphAlignment.LEFT);
                par.setVerticalAlignment(TextAlignment.CENTER);
                run.setText(text);
                break;
            }
            case 2: {
                row.getCell(position).setColor("FFFFFF");
                par.setAlignment(ParagraphAlignment.CENTER);
                par.setVerticalAlignment(TextAlignment.CENTER);
                run.setText(text);
                break;
            }
        }
    }
}
