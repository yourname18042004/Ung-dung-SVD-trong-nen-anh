package SVD;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Test {

    public static SimpleMatrix R, G, B; 

    public static void imageToMatrix(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        R = new SimpleMatrix(height, width);
        G = new SimpleMatrix(height, width);
        B = new SimpleMatrix(height, width);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = img.getRGB(j, i);
                R.set(i, j, (rgb >> 16) & 0xff);
                G.set(i, j, (rgb >> 8) & 0xff); 
                B.set(i, j, rgb & 0xff); 
            }
        }
    }

    public static BufferedImage matrixToImage(SimpleMatrix R, SimpleMatrix G, SimpleMatrix B) {
        int width = R.numCols();
        int height = R.numRows();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int gray = 0;
                
                if(R.get(i, j) != 0.0)
                    gray |= ((int)R.get(i, j) << 16);
             
                if(G.get(i, j) != 0.0)
                    gray |= ((int)G.get(i, j) << 8);

                if(B.get(i, j) != 0.0)
                    gray |= ((int)B.get(i, j));

                img.setRGB(j, i, gray);
            }
        }
        return img;
    }

    public static BufferedImage matrixToImage(SimpleMatrix matrix, int index) {
        int width = matrix.numCols();
        int height = matrix.numRows();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int gray = (int)matrix.get(i, j) << (index * 8);
             
                img.setRGB(j, i, gray);
            }
        }
        return img;
    }
    // Nén ảnh sử dụng SVD
    public static SimpleMatrix compressImage(SimpleMatrix original, int k) {
        SimpleSVD<SimpleMatrix> svd = original.svd();
        SimpleMatrix U = svd.getU();
        SimpleMatrix W = svd.getW();
        SimpleMatrix Vt = svd.getV().transpose();

        // Cắt bớt ma trận W, U, và Vt chỉ giữ k giá trị lớn nhất
        SimpleMatrix Uk = U.extractMatrix(0, U.numRows(), 0, k);
        SimpleMatrix Wk = W.extractMatrix(0, k, 0, k);
        SimpleMatrix Vtk = Vt.extractMatrix(0, k, 0, Vt.numCols());

        // Tính lại ma trận nén
        return Uk.mult(Wk).mult(Vtk);
    }

    public static void main(String[] args) throws IOException {

        // Đọc ảnh từ file
        BufferedImage img = ImageIO.read(new File("Test.png"));

        // Chuyển ảnh sang ma trận
        imageToMatrix(img);
        
        // Phân rã và nén ảnh
        int k = 10;// Số lượng giá trị đặc trưng lớn nhất để giữ

        ToImage(R, 0, "R1.png");
        ToImage(G, 1, "G1.png");
        ToImage(B, 2, "B1.png");

        R = compressImage(R, k);
        G = compressImage(G, k);
        B = compressImage(B, k);
        System.out.println("Ma tran sau khi bien doi :" + R.numRows() + " " + R.numCols() );
        ToImage(R, 0, "R.png");
        ToImage(G, 1, "G.png");
        ToImage(B, 2, "B.png");

        // Chuyển ma trận đã nén trở lại ảnh và lưu ảnh
        try {
            BufferedImage compressedImg = matrixToImage(R, G, B);
            ImageIO.write(compressedImg, "png", new File("compressed_image1.png"));
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public static void ToImage(SimpleMatrix matrix, int i, String path){
        try {
            BufferedImage compressedImg = matrixToImage(matrix, i);
            ImageIO.write(compressedImg, "png", new File(path));
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}

