package org.fusesource.hawtjni.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.fusesource.hawtjni.clang.jaxb.ClangXml;
import org.fusesource.hawtjni.clang.jaxb.Function;

import static org.eclipse.swt.SWT.*;
import static org.fusesource.hawtjni.clang.ClangSupport.*;
import static org.fusesource.hawtjni.ui.IOSupport.*;
import static org.fusesource.hawtjni.ui.UISupport.*;

public class UI {

    public static void main(String[] args) {
        try {
            Display display = new Display();
            Shell shell = new Shell(display);
            UI ui = new UI();
            ui.create(shell);
            shell.open();

            while (!shell.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            display.dispose();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private Text filterText;
    private Table symbolTable;
    private Tree headersTree;
    private Button addHeaderButton;
    private Button removeHeaderButton;

    private Browser browser;
//    private Text hawtJNIText;
//    private Text sourceText;
//    private Text manText;
    private TreeMap<String, Runnable> sortedFunctionTree;

    private void create(Composite parent) {
        buildMainUI(parent);
        attachUIListeners();
    }


    private void buildMainUI(Composite parent) {
        SashForm whole = sashForm(parent, VERTICAL);
        SashForm top = sashForm(whole, HORIZONTAL);
        
        createHeadersPanel(composite(top));
        createSymbolsPanel(composite(top));
        top.setWeights(new int[] {10, 10});
        
        createResultsPanel(composite(whole));
        whole.setWeights(new int[] {10, 10});
    }


    private SashForm sashForm(Composite parent, int vertical) {
        SashForm whole = fill(new SashForm(fill(parent), vertical|SMOOTH));
        whole.setBackground(color(whole, COLOR_GRAY));
        whole.setSashWidth(2);
        return whole;
    }


    private void createHeadersPanel(final Composite parent) {
        parent.setLayout(mig("", "[left, grow]", "[30::]10[grow]10[]"));
        Label label = mig(new Label(parent, NONE), "split 3");
        label.setText("Headers");

        addHeaderButton = mig(new Button(parent, PUSH), "gapleft push");
        addHeaderButton.setText("+");

        removeHeaderButton = mig(new Button(parent, PUSH), "wrap");
        removeHeaderButton.setText("-");
        
        headersTree = new Tree(parent, MULTI | BORDER);
        headersTree.setLayoutData("grow");        
    }

    private void createSymbolsPanel(Composite parent) {
        parent.setLayout(mig("", "[:300:, grow]", "[30]10[grow]"));

        filterText = new Text(parent, SEARCH | ICON_SEARCH | ICON_CANCEL);
        filterText.setLayoutData("growx, wrap");

        symbolTable = new Table(parent, SINGLE | BORDER | CHECK | V_SCROLL | H_SCROLL );
        symbolTable.setLayoutData("span, grow");
    }

    private void createResultsPanel(Composite parent) {
        parent.setLayout(mig("", "[grow]", "[grow]"));
        
        try {
            browser = mig(new Browser(parent, BORDER|MOZILLA), "grow");
        } catch (SWTError e) {
            System.out.println( "Could not instantiate Browser: " + e.getMessage());
            showMessage(parent, "Could not instantiate Browser: " + e.getMessage());
            parent.getDisplay().dispose();
            return;
        }
        
        MozillaSupport.installProtocol("app", "52183e20-4d4b-11de-8a39-0800200c9a32", new MozillaSupport.UriHandler() {
            public InputStream open(URI uri) throws IOException {
                String path = uri.getSchemeSpecificPart();
                return UI.class.getClassLoader().getResourceAsStream("META-INF/app/"+path);
            }
        });
        
//        browser.setUrl("file:/tmp/MobileDevice.log");        
//        browser.setUrl("app:foo");
        
    }


    
    private void showHeaderEntryDialog(final Widget parent) {
        final Shell shell = new Shell(parent.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Header Entry");
        shell.setLayout(mig("", "[right]10[left,grow,200::]", "[]10[]"));

        Label label = mig(new Label(shell, SWT.NONE), "");
        label.setText("Header:");
        final Text text = mig(new Text(shell, SWT.BORDER), "grow,wrap");
        text.setFocus();

        Button cancel = mig(new Button(shell, SWT.PUSH), "tag cancel");
        cancel.setText("Cancel");
        cancel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });

        Button ok = mig(new Button(shell, SWT.PUSH), "tag ok");
        ok.setText("OK");
        ok.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                addHeader(text.getText());
                shell.close();
            }
        });

        shell.setDefaultButton(ok);
        shell.pack();
        shell.open();
    }

    private void showMessage(final Widget parent, String message) {
        final Shell shell = new Shell(parent.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Message");
        shell.setLayout(mig("", "[left,grow,200::]", "[grow]10[]"));

        Label label = mig(new Label(shell, SWT.NONE), "grow");
        label.setText(message);

        Button ok = mig(new Button(shell, SWT.PUSH), "tag ok");
        ok.setText("OK");
        ok.setFocus();
        ok.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });

        shell.setDefaultButton(ok);
        shell.pack();
        shell.open();
    }

    private void attachUIListeners() {
        addHeaderButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                showHeaderEntryDialog(addHeaderButton);
            }
        });

        removeHeaderButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                TreeItem[] selection = headersTree.getSelection();
                for (TreeItem item : selection) {
                    item.dispose();
                    updateFunctionTree();
                }
            }
        });
     
        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                updateFunctionList();
            }
        });

        symbolTable.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                if( event.detail == CHECK ) {
                    System.out.println("checked");
                }
                int index = symbolTable.getSelectionIndex();
                if (index >= 0) {
                    TableItem ti = symbolTable.getItems()[index];
                    ((Runnable) ti.getData()).run();
                }
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

    }

    private void addHeader(String header) {
        ClangXml xml;
        try {
            xml = load(header, null);
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        TreeItem headerItem = new TreeItem(headersTree, SWT.NONE);
        headerItem.setText(header);
        headerItem.setData(xml);

        for (org.fusesource.hawtjni.clang.jaxb.File file : xml.referenceSection.files.files) {
            if ("<built-in>".equals(file.name)) {
                continue;
            }
            TreeItem childItem = new TreeItem(headerItem, SWT.NONE);
            childItem.setText(file.name);
            childItem.setData(file);
        }
        updateFunctionTree();
    }

    private void updateFunctionTree() {
        sortedFunctionTree = new TreeMap<String, Runnable>();
        for (TreeItem item : headersTree.getItems()) {
            final ClangXml xml = (ClangXml) item.getData();
            for (Object unit : xml.translationUnit.units) {
                if (unit.getClass() == Function.class) {
                    final Function f = (Function) unit;
                    if (!sortedFunctionTree.containsKey(f.name)) {
                        sortedFunctionTree.put(f.name, new Runnable() {
                            public void run() {
                                displayFunction(xml, f);
                            }
                        });
                    }
                }
            }
        }

        updateFunctionList();
    }

    private void updateFunctionList() {
        symbolTable.removeAll();
        String filter = filterText.getText().toLowerCase();
        for (Entry<String, Runnable> entry : sortedFunctionTree.entrySet()) {
            String key = entry.getKey();
            if (!filter.isEmpty() && !key.toLowerCase().contains(filter)) {
                continue;
            }
            TableItem ti = new TableItem(symbolTable, SWT.NONE);
            ti.setText(entry.getKey());
            ti.setData(entry.getValue());
        }
    }

    protected void displayFunction(final ClangXml xml, final Function function) {
        final String content = loadFile(function.getFile(xml));
        
        HtmlBuilder builder = new HtmlBuilder() {
            protected void headContent() {
                p("   <base href=\"app:index.html\"></base>");
                p("   <title>Javascript code prettifier</title>");
                p("   <link href='prettify.css' type='text/css' rel='stylesheet' />");
                p("   <script src='prettify.js' type='text/javascript'></script>");
                p("   <link href='style.css' type='text/css' rel='stylesheet' />");
            }

            protected String bodyAttributes() {
                return "onload='prettyPrint()'";
            }
            
            protected void bodyContent() {
                p(div("info",
                    pre("function", esc(function.getCPrototype(xml)))+
                    div("location", "Found at line 53 of /foo/bar/file")
                ));
                p(div("source",
                        div("lines","")+
                        esc(content)
                ));
                p(pre("source", esc(content)));
                
            }

        };
        
        String document = builder.build().toString();
        System.out.println("setting to: "+document);
        browser.setText(document);
        
        
    }

}
