import java.awt.Color;
import java.util.LinkedList;
import edu.princeton.cs.algs4.Picture;
/** this is a typical problem for dynamic programming
 * where DP array dp[i][j] is the total cost from (i, j) to the last row;
 * after calculate for all i & j, find the smallest one in dp[0][j]
 * DP array implicitly indicate the path, by reversely calculating dp[][] with subtraction of energy map
 * */
public class SeamCarver {

    private Picture inPicture;
    private int width;
    private int height;
    private double[][] energyMap;

    public SeamCarver(Picture picture) {
        inPicture = new Picture(picture);
        width = picture.width();
        height = picture.height();
        /** Coordinate of 2D arrary are opposite to that of pixel in picture */
        /** align 2D array to pixel */
        energyMap = new double[width][height];
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                energyMap[x][y] = energy(x, y);
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
    /** save memory space by keeping only one energy map*/
    private double getEnergy(int x, int y, boolean isVertical) {
        if (isVertical) {
            return energyMap[x][y];
        } else {
            return energyMap[y][x];
        }
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
        return dpCalculator(height, width, false);
    }

    public int[] findVerticalSeam() {
        return dpCalculator(width, height, true);
    }

    private int[] dpCalculator(int inWidth, int inHeight, boolean isVertical) {

        double[][] dpArray = new double[inWidth][inHeight];

        for (int y = 0; y < inHeight; y += 1) {
            for (int x = 0; x < inWidth; x += 1) {
                dpArray[x][y] = getEnergy(x, y, isVertical);
                if (y > 0) {
                    if (x == 0) {
                        dpArray[x][y] += Math.min(dpArray[x][y - 1], dpArray[x + 1][y - 1]);
                    } else if (x == inWidth - 1) {
                        dpArray[x][y] += Math.min(dpArray[x - 1][y - 1], dpArray[x][y - 1]);
                    } else {
                        dpArray[x][y] += Math.min(dpArray[x - 1][y - 1], Math.min(dpArray[x][y - 1], dpArray[x + 1][y - 1]));
                    }
                }
            }
        }

        double minCost = dpArray[0][inHeight - 1];
        int minCostIndex = 0;
        for (int x = 1; x < inWidth; x += 1) {
            if (dpArray[x][inHeight - 1] < minCost) {
                minCostIndex = x;
            }
        }
        // the list to keep track index from bottom row to top row
        LinkedList<Integer> minCostPath = new LinkedList<>();
        minCostPath.add(minCostIndex);
        int nextIndex = minCostIndex;
        for (int y = inHeight - 1; y > 0; y -= 1) {
            double dpExclude =  dpArray[nextIndex][y] - getEnergy(nextIndex, y, isVertical);
            if (0 < nextIndex && nextIndex < inWidth - 1) {
                if (dpExclude == dpArray[nextIndex - 1][y - 1]) {
                    nextIndex = nextIndex - 1;
                } else if (dpExclude == dpArray[nextIndex][y - 1]) {
                    nextIndex = nextIndex;
                } else if (dpExclude == dpArray[nextIndex + 1][y - 1]) {
                    nextIndex = nextIndex + 1;
                }
            } else {
                if (nextIndex == inWidth - 1) {
                    if (dpExclude == dpArray[nextIndex - 1][y - 1]) {
                        nextIndex = nextIndex - 1;
                    } else if (dpExclude == dpArray[nextIndex][y - 1]) {
                        nextIndex = nextIndex;
                    }
                } else if (nextIndex == 0) {
                    if (dpExclude == dpArray[nextIndex][y - 1]) {
                        nextIndex = nextIndex;
                    } else if (dpExclude == dpArray[nextIndex + 1][y - 1]) {
                        nextIndex = nextIndex + 1;
                    }
                }
            }
            minCostPath.addFirst(nextIndex);
        }
        int[] outArray = new int[inHeight];
        for (int index = 0; index < inHeight; index += 1) {
            outArray[index] = minCostPath.get(index);
        }
        return outArray;
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
