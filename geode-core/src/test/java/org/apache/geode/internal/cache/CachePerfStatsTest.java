/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.cache;

import static org.apache.geode.internal.cache.CachePerfStats.cacheListenerCallsCompletedId;
import static org.apache.geode.internal.cache.CachePerfStats.cacheWriterCallsCompletedId;
import static org.apache.geode.internal.cache.CachePerfStats.clearsId;
import static org.apache.geode.internal.cache.CachePerfStats.createsId;
import static org.apache.geode.internal.cache.CachePerfStats.deltaFailedUpdatesId;
import static org.apache.geode.internal.cache.CachePerfStats.deltaFullValuesRequestedId;
import static org.apache.geode.internal.cache.CachePerfStats.deltaFullValuesSentId;
import static org.apache.geode.internal.cache.CachePerfStats.deltaGetInitialImagesCompletedId;
import static org.apache.geode.internal.cache.CachePerfStats.deltaUpdatesId;
import static org.apache.geode.internal.cache.CachePerfStats.deltasPreparedId;
import static org.apache.geode.internal.cache.CachePerfStats.deltasSentId;
import static org.apache.geode.internal.cache.CachePerfStats.destroysId;
import static org.apache.geode.internal.cache.CachePerfStats.evictorJobsCompletedId;
import static org.apache.geode.internal.cache.CachePerfStats.evictorJobsStartedId;
import static org.apache.geode.internal.cache.CachePerfStats.getInitialImagesCompletedId;
import static org.apache.geode.internal.cache.CachePerfStats.getTimeId;
import static org.apache.geode.internal.cache.CachePerfStats.getsId;
import static org.apache.geode.internal.cache.CachePerfStats.indexUpdateCompletedId;
import static org.apache.geode.internal.cache.CachePerfStats.invalidatesId;
import static org.apache.geode.internal.cache.CachePerfStats.loadsCompletedId;
import static org.apache.geode.internal.cache.CachePerfStats.missesId;
import static org.apache.geode.internal.cache.CachePerfStats.netloadsCompletedId;
import static org.apache.geode.internal.cache.CachePerfStats.netsearchesCompletedId;
import static org.apache.geode.internal.cache.CachePerfStats.putTimeId;
import static org.apache.geode.internal.cache.CachePerfStats.putallsId;
import static org.apache.geode.internal.cache.CachePerfStats.putsId;
import static org.apache.geode.internal.cache.CachePerfStats.queryExecutionsId;
import static org.apache.geode.internal.cache.CachePerfStats.removeAllsId;
import static org.apache.geode.internal.cache.CachePerfStats.retriesId;
import static org.apache.geode.internal.cache.CachePerfStats.txCommitChangesId;
import static org.apache.geode.internal.cache.CachePerfStats.txCommitsId;
import static org.apache.geode.internal.cache.CachePerfStats.txFailureChangesId;
import static org.apache.geode.internal.cache.CachePerfStats.txFailuresId;
import static org.apache.geode.internal.cache.CachePerfStats.txRollbackChangesId;
import static org.apache.geode.internal.cache.CachePerfStats.txRollbacksId;
import static org.apache.geode.internal.cache.CachePerfStats.updatesId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.Statistics;
import org.apache.geode.StatisticsFactory;
import org.apache.geode.StatisticsType;
import org.apache.geode.internal.cache.CachePerfStats.Clock;
import org.apache.geode.internal.statistics.StatisticsManager;
import org.apache.geode.internal.statistics.StripedStatisticsImpl;

/**
 * Unit tests for {@link CachePerfStats}.
 */
public class CachePerfStatsTest {

  private static final String TEXT_ID = "cachePerfStats";
  private static final long CLOCK_TIME = 10;

  private Statistics statistics;
  private CachePerfStats cachePerfStats;

  @Before
  public void setUp() {
    StatisticsType statisticsType = CachePerfStats.getStatisticsType();

    StatisticsManager statisticsManager = mock(StatisticsManager.class);
    StatisticsFactory statisticsFactory = mock(StatisticsFactory.class);
    Clock clock = mock(Clock.class);

    statistics = spy(new StripedStatisticsImpl(statisticsType, TEXT_ID, 1, 1, statisticsManager));

    when(clock.getTime()).thenReturn(CLOCK_TIME);
    when(statisticsFactory.createAtomicStatistics(eq(statisticsType), eq(TEXT_ID)))
        .thenReturn(statistics);

    CachePerfStats.enableClockStats = true;
    cachePerfStats = new CachePerfStats(statisticsFactory, TEXT_ID, clock);
  }

  @After
  public void tearDown() {
    CachePerfStats.enableClockStats = false;
  }

  @Test
  public void getPutsDelegatesToStatistics() {
    statistics.incInt(putsId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getPuts()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code puts} is to invoke {@code
   * endPut}.
   */
  @Test
  public void endPutIncrementsPuts() {
    cachePerfStats.endPut(0, false);

    assertThat(statistics.getInt(putsId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code puts} currently wraps to negative from max integer value.
   */
  @Test
  public void putsWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(putsId, Integer.MAX_VALUE);

    cachePerfStats.endPut(0, false);

    assertThat(cachePerfStats.getPuts()).isNegative();
  }

  @Test
  public void getGetsDelegatesToStatistics() {
    statistics.incInt(getsId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getGets()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code gets} is to invoke {@code
   * endGet}.
   */
  @Test
  public void endGetIncrementsGets() {
    cachePerfStats.endGet(0, false);

    assertThat(statistics.getInt(getsId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code gets} currently wraps to negative from max integer value.
   */
  @Test
  public void getsWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(getsId, Integer.MAX_VALUE);

    cachePerfStats.endGet(0, false);

    assertThat(cachePerfStats.getGets()).isNegative();
  }

  @Test
  public void getPutTimeDelegatesToStatistics() {
    statistics.incLong(putTimeId, Long.MAX_VALUE);

    assertThat(cachePerfStats.getPutTime()).isEqualTo(Long.MAX_VALUE);

  }

  /**
   * Characterization test: Note that the only way to increment {@code putTime} is to invoke {@code
   * endPut}.
   */
  @Test
  public void endPutIncrementsPutTime() {
    cachePerfStats.endPut(0, false);

    assertThat(statistics.getLong(putTimeId)).isEqualTo(CLOCK_TIME);
  }

  @Test
  public void getGetTimeDelegatesToStatistics() {
    statistics.incLong(getTimeId, Long.MAX_VALUE);

    assertThat(cachePerfStats.getGetTime()).isEqualTo(Long.MAX_VALUE);

  }

  /**
   * Characterization test: Note that the only way to increment {@code getTime} is to invoke {@code
   * endGet}.
   */
  @Test
  public void endGetIncrementsGetTime() {
    cachePerfStats.endGet(0, false);

    assertThat(statistics.getLong(getTimeId)).isEqualTo(CLOCK_TIME);
  }

  @Test
  public void getDestroysDelegatesToStatistics() {
    statistics.incInt(destroysId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getDestroys()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incDestroysIncrementsDestroys() {
    cachePerfStats.incDestroys();

    assertThat(statistics.getInt(destroysId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code destroys} currently wraps to negative from max integer value.
   */
  @Test
  public void destroysWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(destroysId, Integer.MAX_VALUE);

    cachePerfStats.incDestroys();

    assertThat(cachePerfStats.getDestroys()).isNegative();
  }

  @Test
  public void getCreatesDelegatesToStatistics() {
    statistics.incInt(createsId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getCreates()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incCreatesIncrementsDestroys() {
    cachePerfStats.incCreates();

    assertThat(statistics.getInt(createsId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code creates} currently wraps to negative from max integer value.
   */
  @Test
  public void createsWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(createsId, Integer.MAX_VALUE);

    cachePerfStats.incCreates();

    assertThat(cachePerfStats.getCreates()).isNegative();
  }

  @Test
  public void getPutAllsDelegatesToStatistics() {
    statistics.incInt(putallsId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getPutAlls()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code putalls} is to invoke {@code
   * endPutAll}.
   */
  @Test
  public void endPutAllIncrementsDestroys() {
    cachePerfStats.endPutAll(0);

    assertThat(statistics.getInt(putallsId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code putAlls} currently wraps to negative from max integer value.
   */
  @Test
  public void putAllsWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(putallsId, Integer.MAX_VALUE);

    cachePerfStats.endPutAll(0);

    assertThat(cachePerfStats.getPutAlls()).isNegative();
  }

  @Test
  public void getRemoveAllsDelegatesToStatistics() {
    statistics.incInt(removeAllsId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getRemoveAlls()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code removeAlls} is to invoke
   * {@code endRemoveAll}.
   */
  @Test
  public void endRemoveAllIncrementsDestroys() {
    cachePerfStats.endRemoveAll(0);

    assertThat(statistics.getInt(removeAllsId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code removeAlls} currently wraps to negative from max integer value.
   */
  @Test
  public void removeAllsWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(removeAllsId, Integer.MAX_VALUE);

    cachePerfStats.endRemoveAll(0);

    assertThat(cachePerfStats.getRemoveAlls()).isNegative();
  }

  @Test
  public void getUpdatesDelegatesToStatistics() {
    statistics.incInt(updatesId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getUpdates()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code updates} is to invoke {@code
   * endPut}.
   */
  @Test
  public void endPutIncrementsUpdates() {
    cachePerfStats.endPut(0, true);

    assertThat(statistics.getInt(updatesId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code updates} currently wraps to negative from max integer value.
   */
  @Test
  public void updatesWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(updatesId, Integer.MAX_VALUE);

    cachePerfStats.endPut(0, true);

    assertThat(cachePerfStats.getUpdates()).isNegative();
  }

  @Test
  public void getInvalidatesDelegatesToStatistics() {
    statistics.incInt(invalidatesId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getInvalidates()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incInvalidatesIncrementsInvalidates() {
    cachePerfStats.incInvalidates();

    assertThat(statistics.getInt(invalidatesId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code invalidates} currently wraps to negative from max integer value.
   */
  @Test
  public void invalidatesWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(invalidatesId, Integer.MAX_VALUE);

    cachePerfStats.incInvalidates();

    assertThat(cachePerfStats.getInvalidates()).isNegative();
  }

  @Test
  public void getMissesDelegatesToStatistics() {
    statistics.incInt(missesId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getMisses()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code misses} is to invoke {@code
   * endGet}.
   */
  @Test
  public void endGetIncrementsMisses() {
    cachePerfStats.endGet(0, true);

    assertThat(statistics.getInt(missesId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code misses} currently wraps to negative from max integer value.
   */
  @Test
  public void missesWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(missesId, Integer.MAX_VALUE);

    cachePerfStats.endGet(0, true);

    assertThat(cachePerfStats.getMisses()).isNegative();
  }

  @Test
  public void getRetriesDelegatesToStatistics() {
    statistics.incInt(retriesId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getRetries()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incRetriesIncrementsRetries() {
    cachePerfStats.incRetries();

    assertThat(statistics.getInt(retriesId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code retries} currently wraps to negative from max integer value.
   */
  @Test
  public void retriesWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(retriesId, Integer.MAX_VALUE);

    cachePerfStats.incRetries();

    assertThat(cachePerfStats.getRetries()).isNegative();
  }

  @Test
  public void getClearsDelegatesToStatistics() {
    statistics.incInt(clearsId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getClearCount()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incClearCountIncrementsClears() {
    cachePerfStats.incClearCount();

    assertThat(statistics.getInt(clearsId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code clears} currently wraps to negative from max integer value.
   */
  @Test
  public void clearsWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(clearsId, Integer.MAX_VALUE);

    cachePerfStats.incClearCount();

    assertThat(cachePerfStats.getClearCount()).isNegative();
  }

  @Test
  public void getLoadsCompletedDelegatesToStatistics() {
    statistics.incInt(loadsCompletedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getLoadsCompleted()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code loadsCompleted} is to invoke
   * {@code endLoad}.
   */
  @Test
  public void endLoadIncrementsMisses() {
    cachePerfStats.endLoad(0);

    assertThat(statistics.getInt(loadsCompletedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code loads} currently wraps to negative from max integer value.
   */
  @Test
  public void loadsCompletedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(loadsCompletedId, Integer.MAX_VALUE);

    cachePerfStats.endLoad(0);

    assertThat(cachePerfStats.getLoadsCompleted()).isNegative();
  }

  @Test
  public void getNetloadsCompletedDelegatesToStatistics() {
    statistics.incInt(netloadsCompletedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getNetloadsCompleted()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code netloadsCompleted} is to
   * invoke {@code endNetload}.
   */
  @Test
  public void endNetloadIncrementsNetloadsCompleted() {
    cachePerfStats.endNetload(0);

    assertThat(statistics.getInt(netloadsCompletedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code netloadsComplete} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void netloadsCompletedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(netloadsCompletedId, Integer.MAX_VALUE);

    cachePerfStats.endNetload(0);

    assertThat(cachePerfStats.getNetloadsCompleted()).isNegative();
  }

  @Test
  public void getNetsearchesCompletedDelegatesToStatistics() {
    statistics.incInt(netsearchesCompletedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getNetsearchesCompleted()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code netsearchesCompleted} is to
   * invoke {@code endNetsearch}.
   */
  @Test
  public void endLoadIncrementsNetsearchesCompleted() {
    cachePerfStats.endNetsearch(0);

    assertThat(statistics.getInt(netsearchesCompletedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code netsearchesCompleted} currently wraps to negative from max
   * integer value.
   */
  @Test
  public void netsearchesCompletedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(netsearchesCompletedId, Integer.MAX_VALUE);

    cachePerfStats.endNetsearch(0);

    assertThat(cachePerfStats.getNetsearchesCompleted()).isNegative();
  }

  @Test
  public void getCacheWriterCallsCompletedDelegatesToStatistics() {
    statistics.incInt(cacheWriterCallsCompletedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getCacheWriterCallsCompleted()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code cacheWriterCallsCompleted} is
   * to invoke {@code endCacheWriterCall}.
   */
  @Test
  public void endCacheWriterCallIncrementsCacheWriterCallsCompleted() {
    cachePerfStats.endCacheWriterCall(0);

    assertThat(statistics.getInt(cacheWriterCallsCompletedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code cacheWriterCallsCompleted} currently wraps to negative from max
   * integer value.
   */
  @Test
  public void cacheWriterCallsCompletedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(cacheWriterCallsCompletedId, Integer.MAX_VALUE);

    cachePerfStats.endCacheWriterCall(0);

    assertThat(cachePerfStats.getCacheWriterCallsCompleted()).isNegative();
  }

  @Test
  public void getCacheListenerCallsCompletedDelegatesToStatistics() {
    statistics.incInt(cacheListenerCallsCompletedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getCacheListenerCallsCompleted()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code cacheListenerCallsCompleted}
   * is to invoke {@code endCacheListenerCall}.
   */
  @Test
  public void endCacheWriterCallIncrementsCacheListenerCallsCompleted() {
    cachePerfStats.endCacheListenerCall(0);

    assertThat(statistics.getInt(cacheListenerCallsCompletedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code cacheListenerCallsCompleted} currently wraps to negative from max
   * integer value.
   */
  @Test
  public void cacheListenerCallsCompletedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(cacheListenerCallsCompletedId, Integer.MAX_VALUE);

    cachePerfStats.endCacheListenerCall(0);

    assertThat(cachePerfStats.getCacheListenerCallsCompleted()).isNegative();
  }

  @Test
  public void getGetInitialImagesCompletedDelegatesToStatistics() {
    statistics.incInt(getInitialImagesCompletedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getGetInitialImagesCompleted()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code getInitialImagesCompleted} is
   * to invoke {@code endGetInitialImage}.
   */
  @Test
  public void endCacheWriterCallIncrementsGetInitialImagesCompleted() {
    cachePerfStats.endGetInitialImage(0);

    assertThat(statistics.getInt(getInitialImagesCompletedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code getInitialImagesCompleted} currently wraps to negative from max
   * integer value.
   */
  @Test
  public void getInitialImagesCompletedCallsCompletedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(getInitialImagesCompletedId, Integer.MAX_VALUE);

    cachePerfStats.endGetInitialImage(0);

    assertThat(cachePerfStats.getGetInitialImagesCompleted()).isNegative();
  }

  @Test
  public void getDeltaGetInitialImagesCompletedDelegatesToStatistics() {
    statistics.incInt(deltaGetInitialImagesCompletedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getDeltaGetInitialImagesCompleted()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incDeltaGIICompletedIncrementsDeltaGetInitialImagesCompleted() {
    cachePerfStats.incDeltaGIICompleted();

    assertThat(statistics.getInt(deltaGetInitialImagesCompletedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code deltaGetInitialImagesCompleted} currently wraps to negative from
   * max integer value.
   */
  @Test
  public void deltaGetInitialImagesCompletedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(deltaGetInitialImagesCompletedId, Integer.MAX_VALUE);

    cachePerfStats.incDeltaGIICompleted();

    assertThat(cachePerfStats.getDeltaGetInitialImagesCompleted()).isNegative();
  }

  @Test
  public void getQueryExecutionsDelegatesToStatistics() {
    statistics.incInt(queryExecutionsId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getQueryExecutions()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code queryExecutions} is to invoke
   * {@code endQueryExecution}.
   */
  @Test
  public void endQueryExecutionIncrementsQueryExecutions() {
    cachePerfStats.endQueryExecution(1);

    assertThat(statistics.getInt(queryExecutionsId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code queryExecutions} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void queryExecutionsWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(queryExecutionsId, Integer.MAX_VALUE);

    cachePerfStats.endQueryExecution(1);

    assertThat(cachePerfStats.getQueryExecutions()).isNegative();
  }

  @Test
  public void getTxCommitsDelegatesToStatistics() {
    statistics.incInt(txCommitsId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getTxCommits()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code txCommits} is to invoke
   * {@code txSuccess}.
   */
  @Test
  public void txSuccessIncrementsTxCommits() {
    cachePerfStats.txSuccess(1, 1, 1);

    assertThat(statistics.getInt(txCommitsId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code txCommits} currently wraps to negative from max integer value.
   */
  @Test
  public void txCommitsWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(txCommitsId, Integer.MAX_VALUE);

    cachePerfStats.txSuccess(1, 1, 1);

    assertThat(cachePerfStats.getTxCommits()).isNegative();
  }

  @Test
  public void getTxFailuresDelegatesToStatistics() {
    statistics.incInt(txFailuresId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getTxFailures()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code txFailures} is to invoke
   * {@code txFailure}.
   */
  @Test
  public void txFailureIncrementsTxFailures() {
    cachePerfStats.txFailure(1, 1, 1);

    assertThat(statistics.getInt(txFailuresId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code txFailures} currently wraps to negative from max integer value.
   */
  @Test
  public void txFailuresWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(txFailuresId, Integer.MAX_VALUE);

    cachePerfStats.txFailure(1, 1, 1);

    assertThat(cachePerfStats.getTxFailures()).isNegative();
  }

  @Test
  public void getTxRollbacksDelegatesToStatistics() {
    statistics.incInt(txRollbacksId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getTxRollbacks()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code txRollbacks} is to invoke
   * {@code txRollback}.
   */
  @Test
  public void txRollbackIncrementsTxRollbacks() {
    cachePerfStats.txRollback(1, 1, 1);

    assertThat(statistics.getInt(txRollbacksId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code txRollbacks} currently wraps to negative from max integer value.
   */
  @Test
  public void txRollbacksWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(txRollbacksId, Integer.MAX_VALUE);

    cachePerfStats.txRollback(1, 1, 1);

    assertThat(cachePerfStats.getTxRollbacks()).isNegative();
  }

  @Test
  public void getTxCommitChangesDelegatesToStatistics() {
    statistics.incInt(txCommitChangesId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getTxCommitChanges()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code txCommitChanges} is to invoke
   * {@code txSuccess}.
   */
  @Test
  public void txSuccessIncrementsTxCommitChanges() {
    cachePerfStats.txSuccess(1, 1, 1);

    assertThat(statistics.getInt(txCommitChangesId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code txCommitChanges} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void txCommitChangesWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(txCommitChangesId, Integer.MAX_VALUE);

    cachePerfStats.txSuccess(1, 1, 1);

    assertThat(cachePerfStats.getTxCommitChanges()).isNegative();
  }

  @Test
  public void getTxFailureChangesDelegatesToStatistics() {
    statistics.incInt(txFailureChangesId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getTxFailureChanges()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code txFailureChanges} is to
   * invoke {@code txFailure}.
   */
  @Test
  public void txFailureIncrementsTxFailureChanges() {
    cachePerfStats.txFailure(1, 1, 1);

    assertThat(statistics.getInt(txFailureChangesId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code txFailureChanges} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void txFailureChangesWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(txFailureChangesId, Integer.MAX_VALUE);

    cachePerfStats.txFailure(1, 1, 1);

    assertThat(cachePerfStats.getTxFailureChanges()).isNegative();
  }

  @Test
  public void getTxRollbackChangesDelegatesToStatistics() {
    statistics.incInt(txRollbackChangesId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getTxRollbackChanges()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code txRollbackChanges} is to
   * invoke {@code txRollback}.
   */
  @Test
  public void txRollbackIncrementsTxRollbackChanges() {
    cachePerfStats.txRollback(1, 1, 1);

    assertThat(statistics.getInt(txRollbackChangesId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code txRollbackChanges} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void txRollbackChangesWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(txRollbackChangesId, Integer.MAX_VALUE);

    cachePerfStats.txRollback(1, 1, 1);

    assertThat(cachePerfStats.getTxRollbackChanges()).isNegative();
  }

  @Test
  public void getEvictorJobsStartedChangesDelegatesToStatistics() {
    statistics.incInt(evictorJobsStartedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getEvictorJobsStarted()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incEvictorJobsStartedIncrementsEvictorJobsStarted() {
    cachePerfStats.incEvictorJobsStarted();

    assertThat(statistics.getInt(evictorJobsStartedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code evictorJobsStarted} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void evictorJobsStartedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(evictorJobsStartedId, Integer.MAX_VALUE);

    cachePerfStats.incEvictorJobsStarted();

    assertThat(cachePerfStats.getEvictorJobsStarted()).isNegative();
  }

  @Test
  public void getEvictorJobsCompletedChangesDelegatesToStatistics() {
    statistics.incInt(evictorJobsCompletedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getEvictorJobsCompleted()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incEvictorJobsCompletedIncrementsEvictorJobsCompleted() {
    cachePerfStats.incEvictorJobsCompleted();

    assertThat(statistics.getInt(evictorJobsCompletedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code evictorJobsCompleted} currently wraps to negative from max
   * integer value.
   */
  @Test
  public void evictorJobsCompletedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(evictorJobsCompletedId, Integer.MAX_VALUE);

    cachePerfStats.incEvictorJobsCompleted();

    assertThat(cachePerfStats.getEvictorJobsCompleted()).isNegative();
  }

  @Test
  public void getIndexUpdateCompletedChangesDelegatesToStatistics() {
    statistics.incInt(indexUpdateCompletedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getIndexUpdateCompleted()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code indexUpdateCompleted} is to
   * invoke {@code endIndexUpdate}.
   */
  @Test
  public void endIndexUpdateIncrementsEvictorJobsCompleted() {
    cachePerfStats.endIndexUpdate(1);

    assertThat(statistics.getInt(indexUpdateCompletedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code indexUpdateCompleted} currently wraps to negative from max
   * integer value.
   */
  @Test
  public void indexUpdateCompletedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(indexUpdateCompletedId, Integer.MAX_VALUE);

    cachePerfStats.endIndexUpdate(1);

    assertThat(cachePerfStats.getIndexUpdateCompleted()).isNegative();
  }

  @Test
  public void getDeltaUpdatesDelegatesToStatistics() {
    statistics.incInt(deltaUpdatesId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getDeltaUpdates()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code deltaUpdates} is to invoke
   * {@code endDeltaUpdate}.
   */
  @Test
  public void endDeltaUpdateIncrementsDeltaUpdates() {
    cachePerfStats.endDeltaUpdate(1);

    assertThat(statistics.getInt(deltaUpdatesId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code deltaUpdatesId} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void deltaUpdatesWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(deltaUpdatesId, Integer.MAX_VALUE);

    cachePerfStats.endDeltaUpdate(1);

    assertThat(cachePerfStats.getDeltaUpdates()).isNegative();
  }

  @Test
  public void getDeltaFailedUpdatesDelegatesToStatistics() {
    statistics.incInt(deltaFailedUpdatesId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getDeltaFailedUpdates()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incDeltaFailedUpdatesIncrementsDeltaFailedUpdates() {
    cachePerfStats.incDeltaFailedUpdates();

    assertThat(statistics.getInt(deltaFailedUpdatesId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code deltaFailedUpdates} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void deltaFailedUpdatesWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(deltaFailedUpdatesId, Integer.MAX_VALUE);

    cachePerfStats.incDeltaFailedUpdates();

    assertThat(cachePerfStats.getDeltaFailedUpdates()).isNegative();
  }

  @Test
  public void getDeltasPreparedUpdatesDelegatesToStatistics() {
    statistics.incInt(deltasPreparedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getDeltasPrepared()).isEqualTo(Integer.MAX_VALUE);
  }

  /**
   * Characterization test: Note that the only way to increment {@code deltasPrepared} is to invoke
   * {@code endDeltaPrepared}.
   */
  @Test
  public void endDeltaPreparedIncrementsDeltasPrepared() {
    cachePerfStats.endDeltaPrepared(1);

    assertThat(statistics.getInt(deltasPreparedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code deltasPrepared} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void deltasPreparedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(deltasPreparedId, Integer.MAX_VALUE);

    cachePerfStats.endDeltaPrepared(1);

    assertThat(cachePerfStats.getDeltasPrepared()).isNegative();
  }

  @Test
  public void getDeltasSentDelegatesToStatistics() {
    statistics.incInt(deltasSentId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getDeltasSent()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incDeltasSentPreparedIncrementsDeltasSent() {
    cachePerfStats.incDeltasSent();

    assertThat(statistics.getInt(deltasSentId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code deltasSent} currently wraps to negative from max integer value.
   */
  @Test
  public void deltasSentWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(deltasSentId, Integer.MAX_VALUE);

    cachePerfStats.incDeltasSent();

    assertThat(cachePerfStats.getDeltasSent()).isNegative();
  }

  @Test
  public void getDeltaFullValuesSentDelegatesToStatistics() {
    statistics.incInt(deltaFullValuesSentId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getDeltaFullValuesSent()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incDeltaFullValuesSentIncrementsDeltaFullValuesSent() {
    cachePerfStats.incDeltaFullValuesSent();

    assertThat(statistics.getInt(deltaFullValuesSentId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code deltaFullValuesSent} currently wraps to negative from max integer
   * value.
   */
  @Test
  public void deltaFullValuesSentWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(deltaFullValuesSentId, Integer.MAX_VALUE);

    cachePerfStats.incDeltaFullValuesSent();

    assertThat(cachePerfStats.getDeltaFullValuesSent()).isNegative();
  }

  @Test
  public void getDeltaFullValuesRequestedDelegatesToStatistics() {
    statistics.incInt(deltaFullValuesRequestedId, Integer.MAX_VALUE);

    assertThat(cachePerfStats.getDeltaFullValuesRequested()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void incDeltaFullValuesRequestedIncrementsDeltaFullValuesRequested() {
    cachePerfStats.incDeltaFullValuesRequested();

    assertThat(statistics.getInt(deltaFullValuesRequestedId)).isEqualTo(1);
  }

  /**
   * Characterization test: {@code deltaFullValuesRequested} currently wraps to negative from max
   * integer value.
   */
  @Test
  public void deltaFullValuesRequestedWrapsFromMaxIntegerToNegativeValue() {
    statistics.incInt(deltaFullValuesRequestedId, Integer.MAX_VALUE);

    cachePerfStats.incDeltaFullValuesRequested();

    assertThat(cachePerfStats.getDeltaFullValuesRequested()).isNegative();
  }
}
