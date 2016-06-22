package com.boxtrotstudio.ghost.utils.tick;

import com.boxtrotstudio.ghost.utils.CancelToken;
import com.boxtrotstudio.ghost.utils.Global;

import java.util.ArrayList;

public class TickerPool {
    private static int groupSize = 5;
    private static boolean hiresTimer;
    private static ArrayList<TickGroup> groups = new ArrayList<>();

    public static void init(int groupSize, boolean hiresTimer) {
        TickerPool.groupSize = groupSize;
        TickerPool.hiresTimer = hiresTimer;

        if(hiresTimer && System.getProperty("os.name").startsWith("Win")) {
            System.err.println("Windows detected! Applying hi-res timer fix");
            new Thread() {
                {
                    setDaemon(true);
                    start();
                }

                public void run() {
                    while(true) {
                        try {
                            Thread.sleep(Long.MAX_VALUE);
                        }
                        catch(Exception exc) {}
                    }
                }
            };
        }
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
                    updatePriority();
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

        private void updatePriority() {
            thread.setPriority((int) (3 + ((memberCount / (double)groupSize) * (10 - 3))));
        }

        private long lastTime;
        private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("tick-group-" + id);

                while (memberCount > 0) {
                    long start = System.nanoTime();
                    long deltaTime = start - lastTime;
                    lastTime += deltaTime;

                    for (int i = 0; i < tickers.length; i++) {
                        if (tickers[i] == null)
                            continue;

                        if (tickers[i].getToken().isCanceled()) {
                            tickers[i] = null;
                            memberCount--;
                            updatePriority();
                            continue;
                        }

                        try {
                            tickers[i].getTicker().handleTick();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Global.LOGGER.info("Error executing tick!", e);
                        }
                    }


                    if (hiresTimer) {
                        long sleepTime = Math.round((1e9 / 60 - (System.nanoTime() - lastTime)) / 1e6);
                        if (sleepTime <= 0)
                            continue;

                        long prev = System.nanoTime(), diff;
                        while ((diff = (System.nanoTime() - prev) / 1000000) < sleepTime) {
                            if (diff < sleepTime * 0.8) {
                                try {
                                    Thread.sleep(1); //Sleep for the first 4/5 of the time
                                } catch (InterruptedException e) {
                                }
                            } else {
                                Thread.yield(); //Yield this thread for the next 1/5 of the time
                            }
                        }
                    } else {
                        long wait = System.nanoTime() - start;
                        if (wait <= 17) {
                            try {
                                Thread.sleep(17 - wait);
                            } catch (InterruptedException e) {
                            }
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
