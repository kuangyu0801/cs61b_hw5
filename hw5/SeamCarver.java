import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

    private Picture inPicture;
    private int width;
    private int height;
    private double[][] energyMap;
    private double[][] energyMapTrans; // transposed energyMap
    private PathNode[][] pathCostMap;

    public SeamCarver(Picture picture) {
        inPicture = new Picture(picture);
        width = picture.width();
        height = picture.height();
        /** Coordinate of 2D arrary are opposite to that of pixel in picture */
        /** align 2D array to pixel */
        energyMap = new double[width][height];
        energyMapTrans = new double[height][width];
        pathCostMap = new PathNode[width][height];
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
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                energyMapTrans[y][x] = energyMap[x][y];
            }
        }
        return findVerticalSeamHelper(height, width, energyMapTrans);
    }

    public int[] findVerticalSeam() {
        return findVerticalSeamHelper(width, height, energyMap);
    }

    /** PathNode contains both path to the current pixel and the total*/
    private class PathNode implements Comparable<PathNode> {
        private double mCost; // M(i,j)
        private LinkedList<Integer> path;

        private PathNode(double cost, LinkedList<Integer> prevPathList) {
            mCost = cost;
            path = new LinkedList(prevPathList);
        }

        @Override
        public int compareTo(PathNode other) {
            Double diff = new Double(this.mCost - other.mCost);
            return diff.intValue();
        }
    }

    private void pathCostDecider(int x, int y, int inWidth,  double[][] inMap) {
        //System.out.println("[Decide Path For][" + x + "][" + y + "]");
        // row, col index is in pixel coordinate
        if (y == 0) {
            LinkedList<Integer> path = new LinkedList();
            path.add(x);
            pathCostMap[x][y] = new PathNode(inMap[x][y], path);
            return;
        }

        // num of path to choose from
        int numPath = (x == 0 || x == inWidth - 1) ? 2 : 3;
        int xPrevStart = (x == 0) ? 0 : x - 1;
        int yPrev = y - 1;

        LinkedList<PathNode> pathList = new LinkedList();
        for (int i = 0; i < numPath; i += 1) {
            pathList.addLast(pathCostMap[xPrevStart][yPrev]);
            xPrevStart += 1;
        }
        Collections.sort(pathList);
        PathNode selectPathNode = pathList.getFirst();

        LinkedList<Integer> selectPath = new LinkedList(selectPathNode.path);
        selectPath.addLast(x); // add x itself to the selected path
        double newCost = selectPathNode.mCost + inMap[x][y];  // add cost to the selected path cose
        pathCostMap[x][y] = new PathNode(newCost, selectPath);
    }

    // sequence of indices for vertical seam
    private int[] findVerticalSeamHelper(int inWidth, int inHeight, double[][] inMap) {
        /**
         * 1. start from top row, iterate over each pixel from left to right to compute M(i,j)
         * 2. use List to track of the total path to (i,j)
         * 3. List node are the path, which is also a List and cost
         * 4. sorted the list and the node with min cost is assigned to M(i,j)
         * 5. compute next row
         * 6. after last row is computed, find the smallest M(i, height - 1)
         * */

        // row, col index is in pixel coordinate
        for (int y = 0; y < inHeight; y += 1) {
            for (int x = 0; x < inWidth; x += 1) {
                pathCostDecider(x, y, inWidth, inMap);  // align 2D array to pixel
            }
        }

        PathNode minPathNode = pathCostMap[0][inHeight - 1];
        for (int x = 1; x < inWidth; x += 1) {
            if (pathCostMap[x][inHeight - 1].mCost < minPathNode.mCost) {
                minPathNode = pathCostMap[x][inHeight - 1];
            }
        }

        int[] findPath = new int[inHeight];
        Iterator<Integer> pathIterator = minPathNode.path.iterator();
        for (int i = 0; i < inHeight; i += 1) {
            findPath[i] = pathIterator.next();
        }

        //printMap();
        return findPath;
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

    private void printMap() {
        for (int y = 0; y < height; y += 1) {
            for (int x = 0; x < width; x += 1) {
                System.out.print(pathCostMap[x][y].mCost + ",  ");
            }
            System.out.println();
        }

        for (int y = 0; y < height; y += 1) {
            for (int x = 0; x < width; x += 1) {
                System.out.println("(" + x + "," + y + ") |Cost|: "
                        + pathCostMap[x][y].mCost + " |Path|: " + pathCostMap[x][y].path);
            }
            System.out.println();
        }
    }

}
