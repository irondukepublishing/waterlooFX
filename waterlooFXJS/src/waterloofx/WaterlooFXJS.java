/*
 *
 * <http://sigtool.github.io/waterlooFX/>
 *
 * Copyright Malcolm Lidierth 2015-.
 *
 * @author Malcolm Lidierth <a href="https://github.com/sigtool/waterlooFX/issues"> [Contact]</a>
 *
 * waterlooFX is free software:  you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * waterlooFX is distributed in the hope that it will  be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package waterloofx;

import java.awt.EventQueue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import waterloo.fx.plot.AbstractPlot.VisualModel;
import waterloo.fx.plot.Chart;
import waterloo.fx.plot.ScatterPlot;

/**
 *
 * @author ML
 */
public class WaterlooFXJS extends Application {

    private static final AtomicBoolean isFXInitialised = new AtomicBoolean(false);

    private static final String fxmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "\n"
            + "<?import waterloo.fx.plot.*?>\n"
            + "<?import java.lang.*?>\n"
            + "<?import java.util.*?>\n"
            + "<?import javafx.embed.swing.*?>\n"
            + "<?import javafx.scene.*?>\n"
            + "<?import javafx.scene.control.*?>\n"
            + "<?import javafx.scene.shape.*?>\n"
            + "<?import javafx.scene.layout.*?>";

    Pane root;
    Scene scene;
    Stage stage;

    @Override
    public void start(Stage primaryStage) {
        // Optional parameters supplied from dtjava.js
        // Reference to the xml file
        String s = (String) getParameters().getNamed().get("fxml");
        String css = (String) getParameters().getNamed().get("css");

        if (s == null || s.isEmpty()) {
            root = new Pane();
            root.setPrefSize(400, 300);
        } else if (s.startsWith("http://") || s.startsWith("https://")) {
            URL url = null;
            try {
                url = new URL(s);
            } catch (MalformedURLException ex) {
                Text text = new Text(ex.toString());
                text.setWrappingWidth(300);
                root = new FlowPane(text);
            }
            try {
                root = FXMLLoader.load(url);
            } catch (IOException ex) {
                Text text = new Text(ex.toString());
                text.setWrappingWidth(300);
                root = new FlowPane(text);
            }
        } else {
            if (!s.startsWith("fxml/")) {
                s = "fxml/".concat(s);
            }
            if (!s.endsWith(".fxml")) {
                s = s.concat(".fxml");
            }
            try {
                root = FXMLLoader.load(WaterlooFXJS.class.getResource(s));
            } catch (IOException ex) {
                Text text = new Text(ex.toString());
                text.setWrappingWidth(300);
                root = new FlowPane(text);
            }
        }
        scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        stage=primaryStage;
        if (css != null && !css.isEmpty()) {
            scene.getStylesheets().add(css);
        }
    }

    /**
     * Initialises the FX platform: this code is NOT required if launching an
     * app via the start method and executes on the Swing/AWT EventQueue.
     *
     * {@code initFX} needs to be called only once for any instance of the JVM.
     *
     * {@code initFX} create a {@code JFXPanel} - which causes the JavaFX
     * application thread to be initialised. It also calls
     * {@code Platform.setImplicitExit(false)} so that closing all JavaFX Stages
     * will not terminate the application thread.
     *
     * This method is thread-safe and may be called from any thread.
     *
     */
    public static void initFX() {
        if (!isFXInitialised.get()) {
            EventQueue.invokeLater(() -> {
                Platform.setImplicitExit(false);
                new JFXPanel();
                isFXInitialised.set(true);
            });
        }
    }

    public void restage() {
        Platform.runLater(() -> {
            Stage secondaryStage = new Stage();
            secondaryStage.setScene(scene);
            secondaryStage.setOnCloseRequest((WindowEvent event) -> {
                stage.setScene(scene);
                secondaryStage.close();
            });
            secondaryStage.setAlwaysOnTop(true);
            secondaryStage.setTitle("waterlooFX Viewer");
            secondaryStage.show();
        });
    }

    public void setStyle(String id, String style) {
        Platform.runLater(() -> {
            getChart().lookup(id).setStyle(style);
        });
    }

    /**
     * Used here to simulate a Platform.runAndWait which JavaFX Platform does
     * not provide.
     *
     * @param callable the code to run
     * @return the output from the code
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static Object runAndWait(Callable callable) throws InterruptedException, ExecutionException {
        FutureTask<Object> future = new FutureTask(callable);
        Platform.runLater(future);
        return future.get();
    }

    /**
     * Thread-safe and blocking method to invoke {@code lookup()} on the root.
     *
     * Finds this Node, or the first sub-node, based on the given CSS selector.
     * If this node is a Parent, then this function will traverse down into the
     * branch until it finds a match. If more than one sub-node matches the
     * specified selector, this function returns the first of them.
     *
     * The lookup will be performed on the FX Platform thread and the method
     * will return the result once the lookup has completed.
     *
     * @param selector
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @see
     * <a href="http://docs.oracle.com/javase/8/javafx/api/javafx/scene/Node.html#lookup-java.lang.String-">
     * http://docs.oracle.com/javase/8/javafx/api/javafx/scene/Node.html#lookup-java.lang.String-</a>
     *
     *
     */
    public Node lookup(String selector) throws InterruptedException, ExecutionException {
        return (Node) runAndWait(() -> {
            return root.lookup(selector);
        });
    }

    /**
     * Thread-safe and blocking method to invoke {@code lookupAll()} on the
     * root.
     *
     * Finds all Nodes, including the root and any children, which match the
     * given CSS selector. If no matches are found, an empty ArrayList is
     * returned. The list is explicitly unordered.
     *
     * The lookup will be performed on the FX Platform thread and the method
     * will return the result once the lookup has completed.
     *
     * @param selector CSS selector {@code String} e.g. ".plot" or "#myNode"
     * @return an ArrayList of nodes satisfying the {@code selector}
     * @throws InterruptedException
     * @throws ExecutionException
     *
     * @see
     * <a href="http://docs.oracle.com/javafx/2/api/javafx/scene/Node.html#lookupAll(java.lang.String)">
     * http://docs.oracle.com/javafx/2/api/javafx/scene/Node.html#lookupAll(java.lang.String)</a>
     *
     */
    public ArrayList<Node> lookupAll(String selector) throws InterruptedException, ExecutionException {
        return (ArrayList<Node>) runAndWait(() -> {
            Set<Node> n0 = root.lookupAll(selector);
            Node[] n1 = new Node[n0.size()];
            n1 = n0.toArray(n1);
            return new ArrayList(Arrays.asList(n1));
        });
    }

    public boolean isRootVisible() {
        if (root == null) {
            return false;
        } else {
            return root.isVisible();
        }
    }

    public Chart getChart() {
        Node node = root.lookup(".chart");
        if (node == null) {
            return null;
        } else {
            return (Chart) node;
        }
    }

    public URL getURL(String s) throws MalformedURLException {
        return new URL(s);
    }

    public void add(Node node) {
        if (!isRootVisible()) {
            try {
                // If the root is not yet visible, use runAndWait. Seems to be required
                // sometimes for Firefox when a page is first loaded.
                runAndWait(() -> {
                    add(node);
                    return null;
                });
            } catch (InterruptedException | ExecutionException ex) {
            } finally {
                return;
            }
        }
        Platform.runLater(() -> {
            if (getChart() != null) {
                getChart().getChildren().add(node);
                getChart().requestLayout();
            } else {
                root.getChildren().add(node);
                root.requestLayout();
            }
        });
    }

    /**
     * Parses a string of FXML code {@literal (e.g.} the FXML generated by Scene
     * Builder).
     *
     * Returns the root {@code Node} of the created tree. If an exception
     * occurs, a {@code String} will be returned instead.
     *
     * @param fxmlContent the FXML text to be parsed
     * @return A JavaFX {@code Node} or a {@code String} describing an
     * exception.
     */
    public static Object parseFXML(String fxmlContent) {

        // HACKS FOR JAVASCRIPT SUPPORT OF JAVA ENUMS
        fxmlContent = fxmlContent.replace("markerType=", "markerTypeAsString=");

        // Make sure we have the header info for the FXML loader. Can omit this
        // with inlined code in the HTML.
        if (!fxmlContent.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")) {
            fxmlContent = String.format("%s", fxmlHeader).concat(fxmlContent);
        }

//        fxmlContent = fxmlContent.replace("scatterplot", "ScatterPlot");
//        fxmlContent = fxmlContent.replace("lineplot", "LinePlot");
        FXMLLoader loader = new FXMLLoader();
        InputStream stream;
        try {
            //StringReader reader = new StringReader(fxmlContent);
            stream = new ByteArrayInputStream(fxmlContent.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            return "WaterlooFXJS:".concat(ex.toString().concat(fxmlContent));
        }

        Node node;
        try {
            node = loader.load(stream);
        } catch (IOException ex) {
            return "WaterlooFXJS:".concat(ex.toString());
        }
        try {
            stream.close();
        } catch (IOException ex) {
            return "WaterlooFXJS:".concat(ex.toString());
        }
        return node;

    }

    public static VisualModel newVisualModel() {
        return new ScatterPlot().getVisualModel();
    }

//    public void print() {
//        Chart chart = getChart();
//        PrinterJob job = PrinterJob.createPrinterJob();
//        if (job != null && chart != null) {
//            boolean success = job.showPageSetupDialog(null);
//            if (success) {
//                success = job.printPage(chart);
//                if (success) {
//                    job.endJob();
//                }
//            }
//        }
//    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
