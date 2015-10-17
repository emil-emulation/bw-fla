/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.eaas.allocation;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class ClientThreadPool
{
    private static final int                CORE_NUMBER        = Runtime.getRuntime().availableProcessors();
    private static final int                NORM_THREAD_NUMBER = CORE_NUMBER * 3;
    private static final int                MAX_POOL_SIZE      = CORE_NUMBER * 5;
    private static final int                MAX_QUEUE_SIZE     = 100;
    private static final long               KEEP_ALIVE_MIN     = 15;
    private static final ThreadPoolExecutor THREAD_POOL        = new ThreadPoolExecutor(NORM_THREAD_NUMBER, MAX_POOL_SIZE, KEEP_ALIVE_MIN, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(MAX_QUEUE_SIZE), new ThreadPoolExecutor.DiscardPolicy());

    private ClientThreadPool()
    {
        // XXX: NOCODE
    }

    public static int getMaxPoolSize()
    {
        return ClientThreadPool.MAX_POOL_SIZE;
    }
    
    public static Future<?> submit(Runnable task)
    {
        return ClientThreadPool.THREAD_POOL.submit(task);
    }
}