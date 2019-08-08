package paint;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import processcommunitcation.ProcessReaderWriter;
import processcommunitcation.Subscriber;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Base64;
public class MainViewController implements Initializable, Subscriber {

    private  ProcessReaderWriter rw;
    @FXML
    private Canvas canvas;
    @FXML
    Button predictButton;
    @FXML
    private Label resultLabel;
    @FXML
    private ImageView busyIcon;
    private double x,y;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            BufferedImage img=ImageIO.read(this.getClass().getResource("loading.png"));
            Image icon=SwingFXUtils.toFXImage(img,null);
            busyIcon.setImage(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setBusy();
        resultLabel.setText("Loading model please wait...");
        canvas.getGraphicsContext2D().setLineWidth(20.0);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        rw=new ProcessReaderWriter();
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        canvas.getGraphicsContext2D().setStroke(Color.WHITE);
        rw.setSubscriber(this);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY){
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    x = event.getX();
                    y = event.getY();
                    gc.strokeLine(x,y,x,y);
                }
            }
        });
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY){
                    x = event.getX();
                    y = event.getY();
                }
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                GraphicsContext gc = canvas.getGraphicsContext2D();

                gc.strokeLine(x,y,event.getX(),event.getY());
                x= event.getX();
                y= event.getY();
            }
        });
    }

    @FXML
    void quit(ActionEvent event) {
        System.exit(0);
    }
    WritableImage snap;
    @FXML
    void predict(ActionEvent event)  {
        setBusy();
        final WritableImage wi = new WritableImage(400,400);
        final WritableImage snapshot = canvas.snapshot(new SnapshotParameters(),wi);

        Runnable rn =new Runnable() {
            @Override
            public void run() {
                cneterDigit(snapshot);
                SnapshotParameters sp = new SnapshotParameters();
                sp.setFill(Color.TRANSPARENT);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        snap =canvas.snapshot(sp,wi);
                        String base64=Image2Base64(snap);
                        rw.getPrediction(base64);
                    }
                });


            }
        };
        Thread th=new Thread(rn);
        th.start();
     }


    @FXML
    public void clear(ActionEvent action){

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.getGraphicsContext2D().setStroke(Color.WHITE);
    }
    private void cneterDigit(WritableImage snapshot){
        int xs=0;
        int ys=0;
        int m=0;
        for (int i = 0; i < snapshot.getHeight(); i++) {
            for (int j = 0; j < snapshot.getWidth(); j++) {
                if (snapshot.getPixelReader().getArgb(i,j) != -16777216){
                    xs+=i;
                    ys+=j;
                    m++;
                }
            }

        }
        xs/=m;
        ys/=m;
        int dx=200-xs;
        int dy=200-ys;
        ArrayList<String> previousPoints=new ArrayList<>();
        for (int i = 0; i < snapshot.getHeight(); i++) {
            for (int j = 0; j < snapshot.getWidth(); j++) {
                if (snapshot.getPixelReader().getArgb(i,j) != -16777216){
                    final int newx=i+dx;
                    final int newy=j+dy;
                    final int k=i;
                    final int l=j;
                    previousPoints.add(newx+""+newy);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            canvas.getGraphicsContext2D().getPixelWriter().setArgb(newx,newy,-1);
                        }
                    });

                    boolean exist=false;
                    for(String str:previousPoints){
                        exist=str.equals(i+""+j);
                        if(exist)
                            break;
                    }
                    if(!exist)
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                canvas.getGraphicsContext2D().getPixelWriter().setArgb(k,l,-16777216);
                            }
                        });

                }
            }

        }

    }

    private String Image2Base64(WritableImage img){
        ByteArrayOutputStream bi=new ByteArrayOutputStream();
        BufferedImage image =SwingFXUtils.fromFXImage(img,null);
        try {
            ImageIO.write(image,"png",bi);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] array=bi.toByteArray();
        return Base64.getEncoder().encodeToString(array);
    }

    private boolean turn=true;

    private void TurnIcon(){
        Runnable rn =new Runnable() {
            @Override
            public void run() {
                int k=20;
                while(turn){

                  final float j=k;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            busyIcon.setRotate(j);
                        }
                    });
                    k=(k+5)%360;
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread th=new Thread(rn);
        th.start();
    }
    private void setBusy(){
        busyIcon.visibleProperty().set(true);
        turn=true;
        resultLabel.setText("Working...");
        TurnIcon();
        predictButton.setDisable(true);
    }
    private void setReady(){
        busyIcon.visibleProperty().set(false);
        turn=false;
        predictButton.setDisable(false);
    }
    @Override
    public void newMessage(String ms) {
        System.out.println(ms);
        if(ms.equals("ready")){

            Platform.runLater(() -> {
                setReady();
                if(resultLabel.getText().equals("Loading model please wait...")){
                    resultLabel.setText("Ready");
                }

            });
        }else{
            Platform.runLater(() -> {
                resultLabel.setText(ms);

            });


        }

    }
}
