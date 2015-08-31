package me.eddiep.ghost.utils.tick;

import me.eddiep.ghost.utils.CancelToken;

import java.util.ArrayList;

public class TickerPool {
    private static int groupSize = 5;
    private static ArrayList<TickGroup> groups = new ArrayList<>();

    public static void init(int groupSize) {
        TickerPool.groupSize = groupSize;
    }

    public static CancelToken requestTicker(Ticker ticker) {
        for (TickGroup group : groups) {
            if (group.hasSpace()) {
                CancelToken token = group.offer(ticker);
                if (token != null)
                    return token;
            }
        }

        TickGroup newGroup = new TickGroup(groupSize);
        CancelToken token = newGroup.offer(ticker);
        groups.add(newGroup);
        newGroup.start();
        return token;
    }

    private static class TickerMember {
        private Ticker ticker;
        private CancelToken token;

        TickerMember(Ticker ticker, CancelToken token) {
            this.ticker = ticker;
            this.token = token;
        }

        public Ticker getTicker() {
            return ticker;
        }

        public CancelToken getToken() {
            return token;
        }
    }

    private static class TickGroup {
        private TickerMember[] tickers;
        private Thread thread;
        private boolean start;
        private int memberCount = 0;
        private int id;

        public TickGroup(int size) {
            tickers = new TickerMember[size];
            thread = new Thread(runnable);
            this.id = groups.size();
        }

        public int getMemberCount() {
            return memberCount;
        }

        public boolean hasSpace() {
            return memberCount < groupSize;
        }

        public synchronized CancelToken offer(Ticker tickable) {
            if (memberCount >= tickers.length)
                return null;

            CancelToken token = new CancelToken();
            TickerMember ticker = new TickerMember(tickable, token);
            for (int i = 0; i < tickers.length; i++) {
                if (tickers[i] == null) {
                    tickers[i] = ticker;
                    memberCount++;
                    return token;
                }
            }

            return null;
        }

        public void start() {
            if (start)
                return;

            start = true;
            thread.start();
        }

        private int count;
        private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("tick-group-" + id);

                while (memberCount > 0) {
                    long start = System.nanoTime();
                    for (int i = 0; i < tickers.length; i++) {
                        if (tickers[i] == null)
                            continue;

                        if (tickers[i].getToken().isCanceled()) {
                            tickers[i] = null;
                            memberCount--;
                            continue;
                        }

                        tickers[i].getTicker().handleTick();
                    }
                    long dur = (System.nanoTime() - start) / 1000000;

                    if (dur < 16) {
                        try {
                            Thread.sleep(16 - dur);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        count++;
                        if (count > 100) {
                            System.err.println("Took " + dur + "ms to tick!");
                            count = 0;
                        }
                    }
                }

                //System.out.println("[SERVER] Tick group " + id + " is out of members, shutting down");
                start = false;
                groups.remove(TickGroup.this);
            }
        };
    }
}
