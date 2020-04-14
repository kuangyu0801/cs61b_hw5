import java.awt.Color;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    private Picture inPicture;
    private int width;
    private int height;
    private double[][] energyMap;
    private double[][] energyMapTrans; // transposed energyMap

    public SeamCarver(Picture picture) {
        inPicture = picture;
        width = picture.width();
        height = picture.height();
        energyMap = new double[width][height];
        energyMapTrans = new double[height][width];
        for (int i = 0; i < width; i += 1) {
            for (int j = 0; j < height; j += 1) {
                energyMap[i][j] = energy(i, j);
            }
        }
    }

    // current picture
    public Picture picture() {
        return inPicture;
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    private double colorDiff(int x1, int y1, int x2, int y2) {
        Color color1 = inPicture.get(x1, y1);
        Color color2 = inPicture.get(x2, y2);
        double redDiff = color1.getRed() - color2.getRed();
        double greenDiff = color1.getGreen() - color2.getGreen();
        double blueDiff = color1.getBlue() - color2.getBlue();
        return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x > width - 1 || y < 0 || y > height - 1) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        // if x, y in border, then wrap around
        int xRight = (x == width - 1) ? 0 : x + 1;
        int xLeft = (x == 0) ? width - 1 : x - 1;
        int yUp = (y == 0) ? height - 1 : y - 1;
        int yDown = (y == height - 1) ? 0 : y + 1;
        double xDiff = colorDiff(xLeft, y, xRight, y);
        double yDiff = colorDiff(x, yUp, x, yDown);
        return xDiff + yDiff;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        for (int i = 0; i < width; i += 1) {
            for (int j = 0; j < height; j += 1) {
                energyMapTrans[j][i] = energyMap[i][j];
            }
        }
        return findVerticalSeamHelper(height, width, energyMapTrans);
    }

    private int findNextPixel(int x, int y, int inWidth, double[][] inMap) {
        int numNeighbor = (x == 0 || x == inWidth - 1) ? 2 : 3;
        int startIndex = (x == 0) ? 0 : x - 1;
        int findIndex = startIndex;
        double minEnergy = inMap[startIndex][y + 1];
        for (int i = 1; i < numNeighbor; i += 1) {
            if (inMap[startIndex + 1][y + 1] < minEnergy) {
                minEnergy = inMap[startIndex + 1][y + 1];
                findIndex = startIndex + 1;
            }
            startIndex += 1;
        }
        return findIndex;
    }

    public int[] findVerticalSeam() {
        return findVerticalSeamHelper(width, height, energyMap);
    }

    // sequence of indices for vertical seam
    private int[] findVerticalSeamHelper(int inWidth, int inHeight, double[][] inMap) {
        int[][] findArray = new int[inWidth][inHeight];
        double[] minPathCost = new double[inWidth];
        for (int x = 0; x < inWidth; x += 1) {
            for (int depth = 0; depth < inHeight; depth += 1) {
                if (depth == 0) {
                    findArray[x][0] = x;
                    minPathCost[x] = inMap[x][0];
                } else {
                    findArray[x][depth] =
                            findNextPixel(findArray[x][depth - 1], depth - 1, inWidth, inMap);
                    minPathCost[x] += inMap[findArray[x][depth]][depth];
                }
            }
        }
        int indexMinPath = 0;
        double min = minPathCost[0];
        for (int x = 1; x < inWidth; x += 1) {
            if (minPathCost[x] < min) {
                indexMinPath = x;
                min = minPathCost[x];
            }
        }
        return findArray[indexMinPath];
    }

    // remove horizontal seam from picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam.length != width) {
            throw new java.lang.IllegalArgumentException();
        }
        inPicture = SeamRemover.removeHorizontalSeam(inPicture, seam);
        return;
    }

    // remove vertical seam from picture
    public void removeVerticalSeam(int[] seam) {
        if (seam.length != height) {
            throw new java.lang.IllegalArgumentException();
        }
        inPicture = SeamRemover.removeVerticalSeam(inPicture, seam);
        return;
    }

}
