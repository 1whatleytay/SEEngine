package engine;

import java.util.*;

public class SEWrappedObj {
    protected SEObj[] objs;
    protected SERLogic.Range[] drawRanges;
    protected int[] drawRangesStart, drawRangesCount;
    protected String offsetName;
    protected SERLogic.Data matrix;
    protected void genDrawRanges() {
        int maxLength = 0;
        for (SEObj obj : objs) { if (obj.object > maxLength) maxLength = obj.object; }
        boolean[] objsSpace = new boolean[maxLength + 1];
        for (SEObj obj : objs) { objsSpace[obj.object] = true; }
        for (int a = 0; a < objsSpace.length; a++) System.out.print(objsSpace[a] + " ");
        System.out.println();
        ArrayList<SERLogic.Range> ranges = new ArrayList<>();
        boolean isNewRange = false;
        SERLogic.Range cRange = new SERLogic.Range();
        for (int a = 0; a < objsSpace.length; a++) {
            if (!objsSpace[a] || a == objsSpace.length - 1) {
                if (a == objsSpace.length - 1) cRange.count++;
                if (isNewRange) {
                    isNewRange = false;
                    ranges.add(cRange);
                }
                continue;
            }
            if (isNewRange) cRange.count++;
            else { isNewRange = true; cRange = new SERLogic.Range(a, 1); }
        }
        drawRanges = new SERLogic.Range[ranges.size()];
        ranges.toArray(drawRanges);
        drawRangesStart = new int[drawRanges.length]; drawRangesCount = new int[drawRanges.length];
        for (int a = 0; a < drawRanges.length; a++) {
            drawRangesStart[a] = drawRanges[a].start * 4; drawRangesCount[a] = drawRanges[a].count * 4;
        }
    }
}