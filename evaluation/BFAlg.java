package evaluation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import utils.NN;
import utils.TimeIntervalMR;
import utils.Trajectory;

public class BFAlg {
    public long fTime = 0;

    public static Comparator<NN> NNComparator = new Comparator<NN>() {
        @Override
        public int compare(NN p1, NN p2) {
            return p1.sim - p2.sim > 0 ? 1 : -1;
        }
    };

    /**
     * conduct brute-force method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public int getIntersection(ArrayList<Trajectory> queries,
            ArrayList<Trajectory> db) {
        long t1 = System.currentTimeMillis();
        int matchNB = 0;
        ArrayList<TimeIntervalMR> candidates = new ArrayList<>();
        for (Trajectory qTrj : queries) {
            for (Trajectory dbTrj : db) {
                for (int ts = 0; ts < Settings.tsNB - 1; ts++) {
                    TimeIntervalMR mr1 = qTrj.EllipseSeq.get(ts);
                    TimeIntervalMR mr2 = dbTrj.EllipseSeq.get(ts);
                    double[] A = mr1.center;
                    double[] B = mr2.center;
                    double dist = Math.sqrt(Math.pow(A[0] - B[0], 2) + Math.pow(A[1] - B[1], 2));
                    if (dist <= mr1.a + mr2.a) {
                        matchNB++;
                    }
                }
            }
        }
        long t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        return matchNB;
    }

    public ArrayList<Trajectory> rangeSearch(Trajectory qTrj, ArrayList<Trajectory> db, double simThreshold) {
        ArrayList<Trajectory> res = new ArrayList<>();
        for (Trajectory dbTrj : db) {
            double sim = qTrj.simTo(dbTrj);
            if (sim >= simThreshold) {
                res.add(dbTrj);
            }
        }
        return res;
    }

    public int rangeJoin(ArrayList<Trajectory> queries,
            ArrayList<Trajectory> db, double simThreshold) {
        long t1 = System.currentTimeMillis();
        int matchNB = 0;
        int i = 0;
        for (Trajectory qTrj : queries) {
            i++;
            if (i % 100 == 0) {
                System.out.println(i + "/" + queries.size());
            }
            ArrayList<Trajectory> res = rangeSearch(qTrj, db, simThreshold);
            matchNB += res.size();
        }
        long t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        return matchNB;
    }

    public PriorityQueue<NN> nnSearch(ArrayList<Trajectory> db, Trajectory qTrj, int k) {
        PriorityQueue<NN> nnQueue = new PriorityQueue<>(NNComparator);
        for (Trajectory dbTrj : db) {
            double sim = qTrj.simTo(dbTrj);
            if (nnQueue.size() < k) {
                nnQueue.add(new NN(dbTrj, sim));
                continue;
            }
            if (sim > nnQueue.peek().sim) {
                nnQueue.poll();
                nnQueue.add(new NN(dbTrj, sim));
            }
        }
        return nnQueue;
    }

    public void nnJoin(ArrayList<Trajectory> queries, ArrayList<Trajectory> db, int k) {
        long t1 = System.currentTimeMillis();
        for (Trajectory qTrj : queries) {
            PriorityQueue<NN> nn = nnSearch(db, qTrj, k);
            // System.out.println(qTrj.objectID);
            // while (!nn.isEmpty()) {
            // System.out.println(nn.poll());
            // }
            // System.out.println();

        }
        long t2 = System.currentTimeMillis();
        fTime += (t2 - t1);

    }

}