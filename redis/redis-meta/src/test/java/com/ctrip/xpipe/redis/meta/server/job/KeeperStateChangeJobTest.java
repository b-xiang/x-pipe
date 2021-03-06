package com.ctrip.xpipe.redis.meta.server.job;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.runners.MockitoJUnitRunner;
import org.unidal.tuple.Pair;

import com.ctrip.xpipe.api.command.Command;
import com.ctrip.xpipe.redis.core.entity.KeeperMeta;
import com.ctrip.xpipe.redis.meta.server.AbstractMetaServerTest;

/**
 * @author wenchao.meng
 *
 * Jan 4, 2017
 */
@RunWith(MockitoJUnitRunner.class)
public class KeeperStateChangeJobTest extends AbstractMetaServerTest{
	
	private KeeperStateChangeJob job;
	
	private List<KeeperMeta> keepers;
	
	@Mock
	private Command<?> activeSuccessCommand;
	
	
	@Before
	public void beforeKeeperStateChangeJobTest() throws Exception{
		
		keepers = new LinkedList<>();
		
		keepers = createRandomKeepers(2);
		
		
		job = new KeeperStateChangeJob(keepers, new Pair<>("localhost", randomPort()), getXpipeNettyClientKeyedObjectPool(), scheduled);
	}
	
	
	@Test
	public void testHookSuccess() throws Exception{
		
		startServer(keepers.get(0).getPort(), "+OK\r\n");
		startServer(keepers.get(1).getPort(), "+OK\r\n");
		
		job.setActiveSuccessCommand(activeSuccessCommand);

		job.execute().get(2000, TimeUnit.MILLISECONDS);
		
		verify(activeSuccessCommand).execute();
		
	}

	@Test
	public void testHookFail() throws InterruptedException, ExecutionException, TimeoutException{

		job.setActiveSuccessCommand(activeSuccessCommand);

		try{
			job.execute().get(100, TimeUnit.MILLISECONDS);
			Assert.fail();
		}catch(TimeoutException e){
		}
		
		verifyZeroInteractions(activeSuccessCommand);
	}

}
