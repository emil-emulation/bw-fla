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

package de.bwl.bwfla.eaas.allocation.spi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class Lockable
{
	private final ReadWriteLock lock;
	private final Lock wlock;
	private final Lock rlock;


	public Lockable()
	{
		this(true);
	}

	public Lockable(boolean fair)
	{
		this.lock = new ReentrantReadWriteLock(fair);
		this.wlock = lock.writeLock();
		this.rlock = lock.readLock();
	}

	public final void lockShared()
	{
		rlock.lock();
	}

	public final boolean tryLockShared()
	{
		return rlock.tryLock();
	}
	
	public final void unlockShared()
	{
		rlock.unlock();
	}
	
	public final void lock()
	{
		wlock.lock();
	}
	
	public final boolean tryLock()
	{
		return wlock.tryLock();
	}
	
	public final void unlock()
	{
		wlock.unlock();
	}
}
