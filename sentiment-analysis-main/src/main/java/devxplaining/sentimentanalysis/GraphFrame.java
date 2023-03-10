package devxplaining.sentimentanalysis;

import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class GraphFrame extends JFrame { //Classe d'appoggio per avere a disposizione JFrame
    public static void main(String[] args) {  //prova classe
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80,
                    30);
            Object v2 = graph.insertVertex(parent, null, "World!", 240, 150,
                    80, 30);
            graph.insertEdge(parent, null, "Edge", v1, v2);
        } finally {
            graph.getModel().endUpdate();
        }
        mxGraphComponent graphComponent = new mxGraphComponent(graph);

        GraphFrame frame = new GraphFrame();
        frame.add(graphComponent);
        frame.pack();
        frame.setVisible(true);
    }
}