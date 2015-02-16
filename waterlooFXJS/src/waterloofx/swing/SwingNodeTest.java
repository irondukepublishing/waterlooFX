/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waterloofx.swing;

import java.awt.Dimension;
import java.awt.EventQueue;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author ML
 */
public class SwingNodeTest extends Application {
    
    @Override
    public void start(Stage stage) {
//        SwingNode node = new SwingNode();
//        Pane pane= new Pane(node);
//        node.resize(400, 300);
//        EventQueue.invokeLater(() -> {
//            final JPanel p = new JPanel();
//            p.setPreferredSize(new Dimension(400, 300));
//            JButton b = new JButton("This is a button");
//            b.addActionListener((java.awt.event.ActionEvent e) -> {
//                EventQueue.invokeLater(() -> {
//                    p.setBackground(java.awt.Color.red);
//                    p.repaint();
//                });
//            });
//            node.setContent(p);
//            p.add(b);
//            p.revalidate();
//        });
//        Scene scene = new Scene(pane);
//        stage.setScene(scene);
//        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
