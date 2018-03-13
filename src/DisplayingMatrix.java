import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.im4java.core.ConvertCmd;
import org.im4java.core.GMOps;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Stream2BufferedImage;
import org.im4java.process.ProcessStarter;
import org.opencv.core.Core; 
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.util.LeptUtils;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.ITessAPI.ETEXT_DESC;
import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.ITessAPI.TessPageIterator;
import net.sourceforge.tess4j.ITessAPI.TessResultIterator;
import net.sourceforge.tess4j.ITessAPI.TessResultRenderer;
import net.sourceforge.tess4j.ITesseract.RenderedFormat;


public class DisplayingMatrix {

	static Mat processedMat;
	static Mat tempMat;
	static Mat thresMat;
	static Mat morphOpenMat;
	static Mat adaptThresMat;
	static Mat gammaCorrectMat;
	static Mat histoEqualMat;
	static Mat contrastStretchMat;
	static JLabel label;
	static int OPERATION_THRESHOLD = 2;
	static int OPERATION_MORPHOPEN = 3;
	static int OPERATION_ADAPTTHRES = 4;
	static int OPERATION_GAMMACORRECT = 5;
	static int OPERATION_HISTOEQUAL = 6;
	static int OPERATION_CONTRASTSTRETCH = 7;
	static int PreviousOperation = 0;
	static JLabel operationStatus;
	static Mat original;
	
	public static void main(String[] args) {     
	      //Loading the core library 
	      System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

	      
	      label = new JLabel();
	      processedMat = new Mat();
	      
          JFrame frame2 = new JFrame();
          JPanel mainPanel = new JPanel();
          JPanel panel1 = new JPanel();
          panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
          JPanel panel2 = new JPanel();
          JScrollPane scroller = new JScrollPane();
          
          
          JButton filter2dBtn = new JButton("Filter2D");
          JButton resetBtn = new JButton("Reset");
          JButton saveBtn = new JButton("Save Image");
          JButton cropTextBtn = new JButton("Crop All Text");
          JButton histoEqualBtn = new JButton("Histogram Equalize");
          
          operationStatus = new JLabel("Previous operation: Image Captured", JLabel.CENTER);
          
	      //Reading the Image from the file  
          tempMat = Imgcodecs.imread("/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/inputImg.png"
	    		  , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
          processedMat = tempMat;
          ImageIcon rawImg = new ImageIcon(matrix2Img(processedMat));
          label.setIcon(rawImg);
	      
          Mat kernel = new Mat(3,3, CvType.CV_32F){
              {
                  put(0,0,-1);
                  put(0,1,-1);
                  put(0,2,-1);

                  put(1,0,-1);
                  put(1,1,8);
                  put(1,2,-1);

                  put(2,0,-1);
                  put(2,1,-1);
                  put(2,2,-1);
              }
          };
          
          Mat kernel4 = new Mat(3,3, CvType.CV_32F){
              {
                  put(0,0,1);
                  put(0,1,1);
                  put(0,2,1);

                  put(1,0,1);
                  put(1,1,-8);
                  put(1,2,1);

                  put(2,0,1);
                  put(2,1,1);
                  put(2,2,1);
              }
          };
          
          Mat kernel2 = new Mat(3,3, CvType.CV_32F){
              {
                  put(0,0,0);
                  put(0,1,1);
                  put(0,2,0);

                  put(1,0,1);
                  put(1,1,-4);
                  put(1,2,1);

                  put(2,0,0);
                  put(2,1,1);
                  put(2,2,0);
              }
          };
          
          Mat kernel3 = new Mat(3,3, CvType.CV_32F){
              {
                  put(0,0,0);
                  put(0,1,-1);
                  put(0,2,0);

                  put(1,0,-1);
                  put(1,1,4);
                  put(1,2,-1);

                  put(2,0,0);
                  put(2,1,-1);
                  put(2,2,0);
              }
          };
          
          
          resetBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				finalizedEffect();
		        Mat resetMat = Imgcodecs.imread("/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/transformedBmp(5).png"
			    		  , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
				processedMat = resetMat;
	        	ImageIcon imgIcon2 = new ImageIcon(matrix2Img(resetMat));
				label.setIcon(imgIcon2);
				panel2.removeAll();
				panel2.add(label);
				PreviousOperation = 0;
				
			}
        	  
          });
          
          saveBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ImageIcon icon = (ImageIcon) label.getIcon();
				BufferedImage bi = new BufferedImage(
					    icon.getIconWidth(),
					    icon.getIconHeight(),
					    BufferedImage.TYPE_INT_RGB);
					Graphics g = bi.createGraphics();
					// paint the Icon to the BufferedImage.
					icon.paintIcon(null, g, 0,0);
					g.dispose();
					String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

				saveToRes(bi,timeStamp);
			}
        	  
          });
          
          histoEqualBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				//Linear contrast stretching
				//Core.normalize(processedMat,processedMat,0,255,Core.NORM_MINMAX);
				
				// set the number of bins at 256
				MatOfInt histSize = new MatOfInt(256);
				// only one channel
				MatOfInt channels = new MatOfInt(0);
				// set the ranges
				MatOfFloat histRange = new MatOfFloat(0, 256);
				
				// compute the histograms for the B, G and R components
				Mat hist_b = new Mat();
				List<Mat> inputHist = new ArrayList<>();
				inputHist.add(processedMat);
				
				// B component or gray image
				Imgproc.calcHist(inputHist, channels, new Mat(), hist_b, histSize, histRange, false);
				
				int hist_w = 150; // width of the histogram image
				int hist_h = 150; // height of the histogram image
				int bin_w = (int) Math.round(hist_w / histSize.get(0, 0)[0]);
				
				Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC1, new Scalar(0, 0, 0));
				// normalize the result to [0, histImage.rows()]
				Core.normalize(hist_b, hist_b, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
				
				for (int i = 1; i < histSize.get(0, 0)[0]; i++)
				{
					// B component or gray image
					Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_b.get(i - 1, 0)[0])),
							new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);
				}
				
				/*
				MatOfInt histogramSize = new MatOfInt(histSize);

				// Set the height of the histogram and width of the bar.
				int histogramHeight = (int) processedMat.size().height;
				int binWidth = 5;

				// Set the value range.
				MatOfFloat histogramRange = new MatOfFloat(0f, 256f);

				// Create two separate lists: one for colors and one for channels (these will be used as separate datasets).
				Scalar[] colorsRgb = new Scalar[]{new Scalar(255,0,0)};
				MatOfInt[] channels = new MatOfInt[]{new MatOfInt(0)};

				// Create an array to be saved in the histogram and a second array, on which the histogram chart will be drawn.
				Mat[] histograms = new Mat[]{new Mat()};
				Mat histImage = new Mat(150, 150, CvType.CV_8UC1);
				
				

				    Imgproc.calcHist(inputHist, channels[0], new Mat(), histograms[0], histogramSize, histogramRange);
				    Core.normalize(histograms[0], histograms[0], histogramHeight, 0, Core.NORM_INF);
				    for (int j = 0; j < histSize; j++) {
				        Point p1 = new Point(binWidth * (j - 1), histogramHeight - Math.round(histograms[0].get(j - 1, 0)[0]));
				        Point p2 = new Point(binWidth * j, histogramHeight - Math.round(histograms[0].get(j, 0)[0]));
				        Imgproc.line(histImage, p1, p2, colorsRgb[0], 2, 8, 0);
				    
				}
				
				*/

				/*
				finalizedEffect();
	        	
				Imgproc.equalizeHist(processedMat, processedMat);				
				
				//CLAHE clahe = Imgproc.createCLAHE();
                //clahe.apply(imageMat,imageMat);
				*/
				
				ImageIcon imgIcon2 = new ImageIcon(matrix2Img(histImage));
				label.setIcon(imgIcon2);
				panel2.removeAll();
				panel2.add(label);
				PreviousOperation = OPERATION_HISTOEQUAL;
				
				
				
			}
        	  
          });
          
          cropTextBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(PreviousOperation ==4)
				processedMat = adaptThresMat;
				
				
				Mat originalMat = Imgcodecs.imread("/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/transformedBmp(5).png"
			    		  , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
				Mat targetMat = Imgcodecs.imread("/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/"
						+ "best.tif"
			    		  , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
				Mat morphMat = new Mat();
				
				
            	ProcessStarter.setGlobalSearchPath("/opt/local/bin");
            	/*
            	IMOperation op = new IMOperation();
            	op.addImage();
            	op.resample(50);
            	op.addImage("png:-");
            	
            	BufferedImage images = matrix2Img(processedMat);
            	// set up command
            	ConvertCmd convert = new ConvertCmd();
            	Stream2BufferedImage s2b = new Stream2BufferedImage();
            	convert.setOutputConsumer(s2b);

            	// run command and extract BufferedImage from OutputConsumer
            	try {
					convert.run(op,images);
				} catch (IOException | InterruptedException | IM4JavaException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
            	processedMat = bufferedImageToMat(s2b.getImage());
            	*/
				
				Mat morphStructure = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
                Imgproc.morphologyEx(targetMat, morphMat, Imgproc.MORPH_GRADIENT, morphStructure);
                Imgproc.threshold(morphMat,morphMat, 0.0, 255.0, Imgproc.THRESH_OTSU);
                morphStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(9, 1));
                Imgproc.morphologyEx(morphMat, morphMat, Imgproc.MORPH_CLOSE, morphStructure);
                
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(morphMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

				BufferedImage previousBuffImg = matrix2Img(originalMat);
				//System.loadLibrary("tesseract");
				//System.setProperty("jna.library.path", "lib/darwin");
				Tesseract  tessInst = new Tesseract();
				
				TessAPI1 api = new TessAPI1();
				TessBaseAPI handler = api.TessBaseAPICreate();
				
				List<RenderedFormat> list = new ArrayList<RenderedFormat>();
				list.add(RenderedFormat.TEXT);
				List<Word> wordList = null;
				List<Word> finalWordList = new ArrayList<Word>();

				/*
				try {
					
					tessInst.createDocuments("/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/transformedBmp(5).png",
							"/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/OCRText",list);										
					
				} catch (TesseractException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
				
				/*
				tessInst.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO);
				wordList = tessInst.getWords(matrix2Img(originalMat), ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
				System.out.println(wordList.size());
				System.out.println(wordList.get(1).getText());
				FileWriter writer = null;
				try {
					writer = new FileWriter("output.txt");

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 


				for (int i =0 ; i < wordList.size(); i++){
					Word word = wordList.get(i);
					
					try {
						writer.write("Text: \n" + word.getText() + "Confidence: " + word.getConfidence() + "\n\n");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

				try {
					writer.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				*/
				

					        
				try {
					
					Pix inputImg = LeptUtils.convertImageToPix(matrix2Img(originalMat));
					//TessAPI1.TessBaseAPISetInputImage(handler, inputImg);
					//TessAPI1.TessBaseAPISetImage2(handler, inputImg);
					 
					 //TessAPI1.TessBaseAPISetPageSegMode(handler, ITessAPI.TessPageIteratorLevel.RIL_BLOCK);
					 //TessAPI1.TessBaseAPIRecognize(handler, new ETEXT_DESC() );
					 
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				List<String> rectWordList = new ArrayList<>();
				
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
                {
                    MatOfPoint contour = contours.get(contourIdx);

                    MatOfPoint2f new_mat = new MatOfPoint2f(contour.toArray());
                    Imgproc.approxPolyDP(new_mat, approxCurve, contour.total() * 0.05, true);
                    
                    MatOfPoint approxContour = new MatOfPoint();

                    approxCurve.convertTo(approxContour,CvType.CV_32S);
                    Rect rect = Imgproc.boundingRect(approxContour);
                    
                    int padding = 5;
                    
                    Rectangle rectOcr = new Rectangle(rect.x,rect.y,rect.width,rect.height);
                    int x1,y1,x2,y2;
                    x1 = rect.x - padding;
                    y1 = rect.y - padding;
                    x2 = rect.width + padding*2;
                    y2 = rect.height + padding*2;
                    
                    if (rect.x - padding <= 0)
                    	x1 = 1;
		            if (rect.y - padding <= 0)
		            	y1 = 1;
		            
		            if (x1 + rect.width + (padding * 2) > originalMat.width())
		            	x2 = originalMat.width() - x1;
		            if (y1 + rect.height + (padding * 2) > originalMat.height())
		            	y2 = originalMat.height() - y1;
		            
		            Rect biggerRect = new Rect(x1,y1,x2,y2);
                    
                    if (rect.height > 8 && rect.width > 8 && previousBuffImg.getHeight() < 4000) {
                    	
                    	
                    	Mat submat = processedMat.submat(biggerRect);
                    	BufferedImage cropText = matrix2Img(submat);
                    	
                    	
                    	/*
                    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    	
                    	try {
							ImageIO.write( cropText, "jpg", baos );
							baos.flush();
	                    	byte[] imageInByte = baos.toByteArray();
	                    	baos.close();
	                    	final ImageInfo imageInfo = Sanselan.getImageInfo(imageInByte);
	                    	final int physicalHeightDpi = imageInfo.getPhysicalHeightDpi();
	                    	System.out.println("DPI: " + physicalHeightDpi );
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						} catch (ImageReadException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                    	*/

                    	
                    	/*
                    	System.out.println("ImagePixels: " +" "+cropText.getHeight());
                    	System.out.println( "Image length: " + submat.height()/72);
                    	double height = submat.height()/72;
                    	if (height != 0){
                        	int dpi = cropText.getHeight()/(submat.height()/72);
                        	System.out.println("DPI: " + dpi );
                    	}*/

                    	//tessInst.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_LINE);
                    	try {
                    		rectWordList.add(tessInst.doOCR(cropText));
                    		wordList = tessInst.getWords(cropText, ITessAPI.TessPageIteratorLevel.RIL_WORD);
                    		if(wordList.size() > 0)	{
                    			
                    			for (int i =0 ; i < wordList.size(); i++){
                    				
                    				finalWordList.add(wordList.get(i));
                					
                				}
                    		}
                    		//finalWordList.add(wordList.get(0));
                    		
						} catch (TesseractException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                    	
                    	
                    	
                    	//Imgproc.adaptiveThreshold(submat, submat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 5 , 5);
    					// Jagged Edge Removal
    					// To be performed after text line extraction, easier removal when target is big    					
    					//Imgproc.threshold(submat, submat, 125, 255, Imgproc.THRESH_BINARY );
    					
    					//Mat blurredImg = new Mat();
    					//Imgproc.pyrUp(submat, blurredImg);
    					for (int i = 0; i < 1; i++){
    						//Imgproc.medianBlur(blurredImg, blurredImg, 3);
    						//Imgproc.blur(blurredImg, blurredImg, new Size(1,1));]
    						//Imgproc.GaussianBlur(blurredImg, blurredImg, new Size(1,1), 1);
    						
    					}
    					//Imgproc.pyrDown(blurredImg, submat);
    					
    					
    					
                    	//morphStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(2, 2));
                        //Imgproc.morphologyEx(submat, submat, Imgproc.MORPH_CLOSE, morphStructure);
                    	
                        
                        previousBuffImg = joinBufferedImage(cropText,previousBuffImg);

                        //Imgproc.rectangle(originalMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        // new Scalar(255, 0, 255), 2);
                    }
    				
                    
                }
                
                FileWriter writer2 = null;
				try {
					writer2 = new FileWriter("output3.txt");

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 

				/*
				for (int i = 0 ; i < rectWordList.size(); i++){
					
					
					try {
						writer2.write("Text: \n" + rectWordList.get(i)  + "\n");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}*/
				
				for (int i =0 ; i < finalWordList.size(); i++){
					Word word = finalWordList.get(i);
					
					try {
						writer2.write("Text: \n" + word.getText() + "\n" + "Confidence: " + word.getConfidence() + "\n\n");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

				try {
					writer2.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
                /*
                
				Scalar CONTOUR_COLOR = new Scalar(0,255,0);
			    MatOfKeyPoint keypoint = new MatOfKeyPoint();
			    List<KeyPoint> listpoint = new ArrayList<KeyPoint>();
			    KeyPoint kpoint = new KeyPoint();
			    Mat mask = Mat.zeros(originalMat.size(), CvType.CV_8UC1);
			    int rectanx1;
			    int rectany1;
			    int rectanx2;
			    int rectany2;

			    //
			    Scalar zeos = new Scalar(0, 0, 0);
			    List<MatOfPoint> contour1 = new ArrayList<MatOfPoint>();
			    List<MatOfPoint> contour2 = new ArrayList<MatOfPoint>();
			    Mat kernel = new Mat(1, 10, CvType.CV_8UC1, Scalar.all(255));
			    Mat morbyte = new Mat(originalMat.size(), CvType.CV_8UC1);
			    Mat hierarchy = new Mat();
			    Mat keypointMat =  new Mat(originalMat.size(), CvType.CV_8UC1);
			    Mat keyMat =  new Mat();			  
			    

			    Rect rectan2 = new Rect();//
			    Rect rectan3 = new Rect();//
			    int imgsize = originalMat.height() * originalMat.width();
			    //
			        FeatureDetector detector = FeatureDetector
			                .create(FeatureDetector.MSER);
			        detector.detect(processedMat, keypoint);
			        listpoint = keypoint.toList();
			        Features2d.drawKeypoints(originalMat, keypoint, keyMat , new Scalar(0,255,0), 0);
			        Imgproc.cvtColor(keyMat, keyMat, Imgproc.COLOR_BGR2GRAY);
			        
			        for (int ind = 0; ind < listpoint.size(); ind++) {
			            
			        	
			        	kpoint = listpoint.get(ind);
			            rectanx1 = (int) (kpoint.pt.x - 0.5 * kpoint.size);
			            rectany1 = (int) (kpoint.pt.y - 0.5 * kpoint.size);
			            // rectanx2 = (int) (kpoint.pt.x + 0.5 * kpoint.size);
			            // rectany2 = (int) (kpoint.pt.y + 0.5 * kpoint.size);
			            rectanx2 = (int) (kpoint.size);
			            rectany2 = (int) (kpoint.size);
			            if (rectanx1 <= 0)
			                rectanx1 = 1;
			            if (rectany1 <= 0)
			                rectany1 = 1;
			            if ((rectanx1 + rectanx2) > originalMat.width())
			                rectanx2 = originalMat.width() - rectanx1;
			            if ((rectany1 + rectany2) > originalMat.height())
			                rectany2 = originalMat.height() - rectany1;
			            Rect rectant = new Rect(rectanx1, rectany1, rectanx2, rectany2);
			            
			            	if (rectant.area() < 250000){
			            		Mat roi = new Mat(keyMat, rectant);
				                roi.setTo(CONTOUR_COLOR);
			            	}
			                
			        }

			        
	                Mat morphStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(9, 1));
	                //Imgproc.morphologyEx(keyMat, keypointMat, Imgproc.MORPH_OPEN, morphStructure);
	                	                
			        			        
			        Imgproc.morphologyEx(keyMat, keypointMat, Imgproc.MORPH_ERODE, morphStructure);
			        
	                Imgproc.findContours(keypointMat, contour2, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
			        //Imgproc.findContours(keypointMat, contour2, hierarchy,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
			        
			        
			        for (int ind = 0; ind < contour2.size(); ind++) {
			        					        
			            rectan3 = Imgproc.boundingRect(contour2.get(ind));
			            //Imgproc.rectangle(originalMat, rectan3.br(), rectan3.tl(), CONTOUR_COLOR);
			            
			            System.out.println("Rectangle area: " + rectan3.area() );
			            
			            if (rectan3.area() > 0.5 * imgsize || rectan3.area() < 50
			                    || rectan3.width / rectan3.height < 2) {
			                Mat roi = new Mat(morbyte, rectan3);
			                roi.setTo(zeos);

			            } else
			                Imgproc.rectangle(originalMat, rectan3.br(), rectan3.tl(),
			                        CONTOUR_COLOR);
			        }
			        
			        */

                ImageIcon imgIcon = new ImageIcon(previousBuffImg);
				label.setIcon(imgIcon);
				panel2.removeAll();
				panel2.add(label);
			}
        	  
          });
          
          filter2dBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
		          
					finalizedEffect();

					Mat imageMat = Imgcodecs.imread("/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/transformedBmp(5).png"
				    		  , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
								
					imageMat = processedMat;
					Imgproc.equalizeHist(imageMat, imageMat);
					//imageMat.convertTo(imageMat, CvType.CV_8UC1);
					//Imgproc.bilateralFilter(imageMat, imageMat, 5, 80, 80, Core.BORDER_DEFAULT);
					
					//Filter2D-Kernel4 Operation
					//
					Mat temp = new Mat();
					Mat absLaplace = new Mat();
					
					//Imgproc.GaussianBlur(processedMat, temp, new Size(3,3), 0);
					Mat LaplaceImg = new Mat();
                    //Imgproc.Laplacian(imageMat,LaplaceImg,3);
                    //Core.convertScaleAbs(LaplaceImg,absLaplace,1,0);
                    Imgproc.filter2D(imageMat,LaplaceImg,3,kernel4);
                    LaplaceImg.convertTo(LaplaceImg, CvType.CV_8UC1);
                    Core.convertScaleAbs(LaplaceImg,absLaplace,1,0);
                    Core.subtract(imageMat,absLaplace,temp);
                    //processedMat = temp;
                    
                    // Canny-Contour-Approx Operation
                    //
					Mat imageMat3 = Imgcodecs.imread("/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/transformedBmp(5).png"
				    		  , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                    Mat originalMat = imageMat3;
                    Imgproc.Canny(originalMat, originalMat, 80, 200);
    				List<MatOfPoint> contours = new ArrayList<>();
    				Imgproc.findContours(originalMat, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
    				
    				MatOfPoint2f approxCurve = new MatOfPoint2f();
    				List<MatOfPoint> mContours = new ArrayList<>();
                    for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
                    {
                        MatOfPoint contour = contours.get(contourIdx);

                        MatOfPoint2f new_mat = new MatOfPoint2f(contour.toArray());
                        Imgproc.approxPolyDP(new_mat, approxCurve, contour.total() * 0.05, true);
                        
                        MatOfPoint approxContour = new MatOfPoint();

                        approxCurve.convertTo(approxContour,CvType.CV_32S);
                        
                        mContours.add(approxContour);
                        

                    }
                    
                    Mat mask = Mat.zeros(originalMat.size(), CvType.CV_8UC1);
                    Imgproc.drawContours(mask, mContours, -1, new Scalar(255, 255, 255), 1);
                    Mat contourmat = new Mat();
                    Core.add(imageMat3,mask,contourmat);
                    processedMat = contourmat;

                    
                    //Filter2d-Kernel2 Operation
                    //
                    Mat kernel2Mat = new Mat();
					Mat absKernel2 = new Mat();
                    Mat imageMat4 = Imgcodecs.imread("/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/transformedBmp(5).png"
				    		  , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

					Mat LaplaceKernel2 = new Mat();
                    Imgproc.filter2D(imageMat4,LaplaceKernel2,3,kernel2);
                    LaplaceKernel2.convertTo(LaplaceKernel2, CvType.CV_8UC1);
                    Core.convertScaleAbs(LaplaceKernel2,absKernel2,1,0);
                    Core.subtract(imageMat4,absKernel2,kernel2Mat);
					
                    //Sobel Edge Operation
                    //
                    Mat imageMat2 = Imgcodecs.imread("/Users/thyemunchun/Documents/Eclipseworkspace/OCRTuningOpenCV/Resources/transformedBmp(5).png"
				    		  , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
					Mat grad_x = new Mat();
					Mat grad_y = new Mat();
					Mat abs_grad_x = new Mat();
					Mat abs_grad_y = new Mat();
					Mat output = new Mat();
					Mat productMat = new Mat();
					int scale = 1;
					int delta = 0;
					int ddepth = CvType.CV_16S; 
					
					
					
					//Imgproc.Scharr(temp, grad_x, temp.depth(), 1, 0, scale, delta);
					Imgproc.Sobel(imageMat2, grad_x, ddepth, 1, 0, 3, scale, delta );
					Core.convertScaleAbs(grad_x,abs_grad_x,1,0);
                    
					//Imgproc.Scharr(temp, grad_x, temp.depth(), 0, 1, scale, delta);
					Imgproc.Sobel(imageMat2, grad_y, ddepth, 0, 1, 3, scale, delta );			
					Core.convertScaleAbs(grad_y,abs_grad_y,1,0);

					Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, output);
					
					// Spatial Enchancement extension
					Imgproc.blur(output, output, new Size(5,5));
					Core.multiply(temp, output, productMat);
					Mat addMat = new Mat();
					Core.add(imageMat2, productMat, addMat);			
					
					//Sobel OTSU extension					
					//Mat newmat = new Mat();
					//Core.subtract(imageMat2,output,newmat);
					//processedMat.convertTo(processedMat, CvType.CV_8UC1);
					//Imgproc.threshold(newmat, newmat, 0.0, 255.0, Imgproc.THRESH_OTSU);
					processedMat = addMat;
					
					
					// Display Image
					//
		        	ImageIcon imgIcon = new ImageIcon(matrix2Img(processedMat));
					label.setIcon(imgIcon);
					panel2.removeAll();
					panel2.add(label);
		        	PreviousOperation = 1;
		        	
			}
        	  
          });
        
          
          JSlider thresholdSlider = new JSlider(JSlider.HORIZONTAL);
          thresholdSlider.setMaximum(255);
          thresholdSlider.setMinimum(0);
          thresholdSlider.setValue(100);
          JLabel status = new JLabel("Current Threshold value is: 100", JLabel.CENTER);
          thresholdSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				status.setText("Current Threshold value is: " + ((JSlider)e.getSource()).getValue());
				System.out.println("Triggered" );
				
				if (((JSlider)e.getSource()).getValue() % 2 != 0){
					thresMat = new Mat();
					
					if(PreviousOperation != OPERATION_THRESHOLD){
						finalizedEffect();
					}
					
				Imgproc.threshold(processedMat, thresMat, ((JSlider)e.getSource()).getValue(), 
						255, Imgproc.THRESH_BINARY );
				
											
				ImageIcon imgIcon = new ImageIcon(matrix2Img(thresMat));
				label.setIcon(imgIcon);
				panel2.remove(0);
				panel2.add(label);
				PreviousOperation = OPERATION_THRESHOLD;

				System.out.println("inside If loop" );	
				}
			}
          });
          
          JSlider thresholdINVSlider = new JSlider(JSlider.HORIZONTAL);
          thresholdINVSlider.setMaximum(255);
          thresholdINVSlider.setMinimum(0);
          thresholdINVSlider.setValue(100);
          JLabel thresholdINVStatus = new JLabel("Current Inverse Threshold value is: 100", JLabel.CENTER);
          thresholdINVSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				thresholdINVStatus.setText("Current Inverse Threshold value is: " + ((JSlider)e.getSource()).getValue());
				
				if (((JSlider)e.getSource()).getValue() % 2 != 0){
					thresMat = new Mat();
					
					if(PreviousOperation != OPERATION_THRESHOLD){
						finalizedEffect();
					}
					
				Imgproc.threshold(processedMat, thresMat, ((JSlider)e.getSource()).getValue(), 
						255, Imgproc.THRESH_BINARY_INV );
				
				ImageIcon imgIcon = new ImageIcon(matrix2Img(thresMat));
				label.setIcon(imgIcon);
				panel2.remove(0);
				panel2.add(label);
				PreviousOperation = OPERATION_THRESHOLD;

				}
			}
          });
          

          JSlider morphOpenSlider = new JSlider(JSlider.HORIZONTAL);
          morphOpenSlider.setMaximum(23);
          morphOpenSlider.setMinimum(1);
          morphOpenSlider.setValue(1);
          JLabel morphOpenStatus = new JLabel("Current Morphology Open value is: 1", JLabel.CENTER);
          morphOpenSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				morphOpenStatus.setText("Current Morphology Open value is: " + ((JSlider)e.getSource()).getValue());
				
				if (((JSlider)e.getSource()).getValue() % 2 != 0){
					morphOpenMat = new Mat();
					
					if(PreviousOperation != OPERATION_MORPHOPEN){
						finalizedEffect();
					}
					
					Mat morphStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, 
							new Size(((JSlider)e.getSource()).getValue(),((JSlider)e.getSource()).getValue()));
			        //Imgproc.morphologyEx(processedMat, morphOpenMat, Imgproc.MORPH_CLOSE, morphStructure);
					Mat morphStructureDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, 
							new Size(((JSlider)e.getSource()).getValue(),((JSlider)e.getSource()).getValue()));			  
			        Imgproc.erode(processedMat,morphOpenMat,morphStructureDilate);
				
				ImageIcon imgIcon = new ImageIcon(matrix2Img(morphOpenMat));
				label.setIcon(imgIcon);
				panel2.remove(0);
				panel2.add(label);
				PreviousOperation = OPERATION_MORPHOPEN;

				}
			}
          });
          
          JSlider adaptThresSlider = new JSlider(JSlider.HORIZONTAL);
          adaptThresSlider.setMaximum(53);
          adaptThresSlider.setMinimum(1);
          adaptThresSlider.setValue(7);
          JLabel adaptThresStatus = new JLabel("Current Adaptive Threshold value is: 7", JLabel.CENTER);
          adaptThresSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				adaptThresStatus.setText("Current Adaptive Threshold value is: " + ((JSlider)e.getSource()).getValue());
				
				if (((JSlider)e.getSource()).getValue() % 2 != 0){
					adaptThresMat = new Mat();
					
					if(PreviousOperation != OPERATION_ADAPTTHRES){
						finalizedEffect();
					}
					
			          Imgproc.adaptiveThreshold(processedMat, adaptThresMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 
			        		  Imgproc.THRESH_BINARY, ((JSlider)e.getSource()).getValue(), 5);

				ImageIcon imgIcon = new ImageIcon(matrix2Img(adaptThresMat));
				label.setIcon(imgIcon);
				panel2.remove(0);
				panel2.add(label);
				PreviousOperation = OPERATION_ADAPTTHRES;

				}
			}
          });
          
          JSlider gammaCorrectionSlider = new JSlider(JSlider.HORIZONTAL);
          gammaCorrectionSlider.setMaximum(20);
          gammaCorrectionSlider.setMinimum(0);
          gammaCorrectionSlider.setValue(10);
          JLabel gammaValueStatus = new JLabel("Current Gamma Correction value is: 1", JLabel.CENTER);
          gammaCorrectionSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				gammaValueStatus.setText("Current Gamma Correction value is: " + ((JSlider)e.getSource()).getValue());
				
					gammaCorrectMat = new Mat();
					if(PreviousOperation != OPERATION_GAMMACORRECT){
						finalizedEffect();
					}
					processedMat.convertTo(processedMat, CvType.CV_32F);
			        Core.pow(processedMat, ((JSlider)e.getSource()).getValue()* 0.1, gammaCorrectMat);			
			        Core.convertScaleAbs(gammaCorrectMat,gammaCorrectMat,1,0);
			        

				ImageIcon imgIcon = new ImageIcon(matrix2Img(gammaCorrectMat));
				label.setIcon(imgIcon);
				panel2.remove(0);
				panel2.add(label);
				PreviousOperation = OPERATION_GAMMACORRECT;

				
			}
          });
          
          JSlider contrastStretchSlider = new JSlider(JSlider.HORIZONTAL);
          contrastStretchSlider.setMaximum(255);
          contrastStretchSlider.setMinimum(0);
          contrastStretchSlider.setValue(100);
          JLabel contrastStretchstatus = new JLabel("Current Contrast Stretching starting value is: 100", JLabel.CENTER);
          contrastStretchSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				contrastStretchstatus.setText("Current Contrast Stretching starting value is: " + ((JSlider)e.getSource()).getValue());
				

					contrastStretchMat = processedMat.clone();
	
					
					
					if(PreviousOperation != OPERATION_CONTRASTSTRETCH){
						finalizedEffect();
					}
				
					
					for (int i = 0; i < processedMat.rows(); i++) { 
						
						for (int j = 0; j < processedMat.cols(); j++) {
							double[] inputMat = processedMat.get(i, j);
							
							double[] outputMat = contrastStretching( (int) inputMat[0],((JSlider)e.getSource()).getValue());
							contrastStretchMat.put(i, j, outputMat);
							//processedMat.type();
						}
					}
									
											
				ImageIcon imgIcon = new ImageIcon(matrix2Img(contrastStretchMat));
				label.setIcon(imgIcon);
				panel2.remove(0);
				panel2.add(label);
				PreviousOperation = OPERATION_CONTRASTSTRETCH;

				
			}
          });



          //Set Content to the JFrame      
          panel1.add(operationStatus);
          panel1.add(thresholdSlider);
          panel1.add(status);
          panel1.add(thresholdINVSlider);
          panel1.add(thresholdINVStatus);
          panel1.add(morphOpenSlider);
          panel1.add(morphOpenStatus);
          panel1.add(adaptThresSlider);
          panel1.add(adaptThresStatus);
          panel1.add(gammaCorrectionSlider);
          panel1.add(gammaValueStatus);
          panel1.add(contrastStretchSlider);
          panel1.add(contrastStretchstatus);
          
          
          panel1.add(filter2dBtn);
          panel1.add(resetBtn);
          panel1.add(saveBtn);
          panel1.add(cropTextBtn);
          panel1.add(histoEqualBtn);
          
          
          panel2.add(label);
          scroller.setViewportView(panel2);
          scroller.getVerticalScrollBar().setUnitIncrement(16);
          //scroller.getViewport().setPreferredSize(new Dimension(500, 300));
          scroller.setAutoscrolls(true);

          mainPanel.add(scroller);
          mainPanel.add(panel1);
          frame2.getContentPane().add(mainPanel);
          frame2.pack(); 
          frame2.setVisible(true);
          frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


	      //Printing the matrix 
	      //System.out.println("OpenCV Mat data:\n" + matrix.dump()); 
	   } 
	
    

    private static void finalizedEffect() {
    	
    	switch(PreviousOperation) {
    	
    	case 0:
    		operationStatus.setText("Previous operation: Image Captured");
    		break;
    	
    	case 1:
    		operationStatus.setText("Previous Operation: Filter2d");
    		break;
    		
    	case 2:
    		processedMat = thresMat;
    		operationStatus.setText("Previous Operation: Threshold");
    		break;
    		
    	case 3:
    		processedMat = morphOpenMat;
    		operationStatus.setText("Previous Operation: Morphology Open");
    		break;
    		
    	case 4:
    		processedMat = adaptThresMat;
    		
    		operationStatus.setText("Previous Operation: Adaptive Threshold");
    		break;
    	case 5:
    		processedMat = gammaCorrectMat;
    		operationStatus.setText("Previous Operation: Gamma Correction");
    		break;
    	case 6:
    		//processedMat = histoEqualMat;
    		operationStatus.setText("Previous Operation: Histogram Equalization");
    		break;
    	case 7:
    		processedMat = contrastStretchMat;
    		operationStatus.setText("Previous Operation: Contrast Stretching");
    		break;
    	}
    	
    	
    	return;
    }
    
    private static double[] contrastStretching(int x, int r1){
    	
    	int s1, r2, s2;
    	
    	r2 = r1 + 60;
    	s1 = 25;
    	s2 = 230;
    	
    	double[] result = {0};
		//double[] result = null;
    	if (0<=x && x <= r1) {
    		result[0] = (s1/r1)*x;
    	}
    	else if (r1 < x && x < r2) {
    		result[0] = ((s2-s1)/(r2-r1)) * (x-r1) + s1;
    	}
    	else if (r2 < x && x < 255) {
    		result[0] = ((255-s2)/(255-r2)) * (x-r2) + s2;
    	}
    		
    	
		return  result;
    	
    }
    
    private static void saveToRes(BufferedImage img, String name){
    	
    	File outputfile = new File(name + ".png");
    	try {
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return;
    }
    
    public static Mat bufferedImageToMat(BufferedImage bi) {
    	  Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
    	  byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
    	  mat.put(0, 0, data);
    	  return mat;
    	}
	
	private static BufferedImage matrix2Img(Mat inputMat){
        MatOfByte matOfByte2 = new MatOfByte();
        Imgcodecs.imencode(".png", inputMat, matOfByte2);
        
        byte[] byteArray2 = matOfByte2.toArray();
        InputStream in2 = new ByteArrayInputStream(byteArray2); 
        BufferedImage bufImage2 = null;
		try {
			bufImage2 = ImageIO.read(in2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return bufImage2;
	}
	
	public static BufferedImage joinBufferedImage(BufferedImage img1,BufferedImage img2) {

        //do some calculate first
        int offset  = 5;
        int wid = Math.max(img1.getWidth(),img2.getWidth())+offset;
        int height = img1.getHeight()+img2.getHeight()+offset;
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(wid,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        //Color oldColor = g2.getColor();
        //fill background
        //g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, wid, height);
        //draw image
        //g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, 0, img1.getHeight()+offset);
        g2.dispose();
        return newImage;
    }
	
}

//Access image within Resources folder
//