package org.pestinha.android;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class UsageLedgerTest {
    private UsageLedger ledger(){return new UsageLedger(0,new HashSet<>(Arrays.asList("instagram")));}
    @Test public void switchingAwayStopsSessionAndDoesNotStoreOtherApps(){UsageLedger l=ledger();l.accept(1000,"instagram",1);l.accept(61000,"browser",1);UsageLedger.Result r=l.finish(121000,true);assertNull(r.current);assertEquals(60000L,(long)r.totals.get("instagram"));assertFalse(r.totals.containsKey("browser"));}
    @Test public void sessionAndDailyTotalAreDifferent(){UsageLedger l=ledger();l.accept(1000,"instagram",1);l.accept(61000,"instagram",2);l.accept(121000,"instagram",1);UsageLedger.Result r=l.finish(181000,true);assertEquals(60000,r.sessionMillis);assertEquals(120000L,(long)r.totals.get("instagram"));}
    @Test public void screenOffAndLockDoNotCountAsUse(){UsageLedger l=ledger();l.accept(1000,"instagram",1);l.accept(61000,"",16);UsageLedger.Result r=l.finish(361000,false);assertNull(r.current);assertEquals(60000L,(long)r.totals.get("instagram"));}
    @Test public void resumeFromSleepStartsAnotherSession(){UsageLedger l=ledger();l.accept(1000,"instagram",1);l.accept(61000,"",17);l.accept(121000,"instagram",1);assertEquals(30000,l.finish(151000,true).sessionMillis);}
    @Test public void duplicateForegroundDoesNotDoubleCount(){UsageLedger l=ledger();l.accept(1000,"instagram",1);l.accept(30000,"instagram",1);UsageLedger.Result r=l.finish(61000,true);assertEquals(60000,r.sessionMillis);assertEquals(60000L,(long)r.totals.get("instagram"));}
    @Test public void activityChangeWithinOneAppKeepsSessionButNotGapInDailyTotal(){UsageLedger l=ledger();l.accept(1000,"instagram",1);l.accept(61000,"instagram",2);l.accept(62000,"instagram",1);UsageLedger.Result r=l.finish(122000,true);assertEquals(121000,r.sessionMillis);assertEquals(120000L,(long)r.totals.get("instagram"));}
    @Test public void midnightClampsTodaysTotals(){UsageLedger l=new UsageLedger(100000,new HashSet<>(Arrays.asList("instagram")));l.accept(90000,"instagram",1);UsageLedger.Result r=l.finish(120000,true);assertEquals(20000,r.sessionMillis);assertEquals(20000L,(long)r.totals.get("instagram"));}
}
