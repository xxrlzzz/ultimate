/* $Id: Phase.java 307 2008-07-29 16:03:20Z jfaber $
 *
 * This file is part of the PEA tool set
 *
 * The PEA tool set is a collection of tools for Phase Event Automata
 * (PEA).
 *
 * Copyright (C) 2005-2006, Carl von Ossietzky University of Oldenburg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package de.uni_freiburg.informatik.ultimate.lib.pea;

import java.util.*;

import de.uni_freiburg.informatik.ultimate.lib.pea.util.SimpleSet;

public class Phase implements Comparable<Phase> {
	int nr;

	// SR 2010-07-09
	private final boolean isKernel;
	public boolean isInit;
	private final boolean isEntry;
	private final boolean isExit;
	private final Vector<Transition> incomming;
	String name;
	CDD stateInv;
	CDD clockInv;
	Set<String> stoppedClocks;
	List<Transition> transitions;
	public int ID;
	Map<CDD, Boolean> phantoms;

	/**
	 * The phase bits used by the powerset construction. This is only set for automata built from CounterExample traces.
	 */
	PhaseBits phaseBits;

	public Phase(final String name, final CDD stateInv, final CDD clockInv, final Set<String> stoppedClocks) {
		this.name = name;
		this.stateInv = stateInv;
		this.clockInv = clockInv;
		transitions = new ArrayList<>();
		this.stoppedClocks = stoppedClocks;

		isKernel = false;
		isInit = false;
		isEntry = false;
		isExit = false;
		incomming = new Vector<>();
		phantoms = new HashMap<>();
	}

	public Phase(final String name, final CDD stateInv, final CDD clockInv) {
		this(name, stateInv, clockInv, new SimpleSet<String>(0));
	}

	public Phase(final String name, final CDD stateInv) {
		this(name, stateInv, CDD.TRUE);
	}

	public Phase(final String name) {
		this(name, CDD.TRUE, CDD.TRUE);
	}

	public boolean isInit() {
		return isInit;
	}

	public void setInit(final boolean isInit) {
		this.isInit = isInit;
	}

	public PhaseBits getPhaseBits() {
		return phaseBits;
	}

	public CDD getStateInvariant() {
		return stateInv;
	}

	public CDD getStateInvNoPhantom() {
		CDD baseStateInv = stateInv;
		for (CDD cdd : phantoms.keySet()) {
			String opName = phantoms.get(cdd) ? "pc" : "p";
			baseStateInv = baseStateInv.assume(cdd.operator(opName));
		}
		return baseStateInv;
	}

	public void setStateInvariant(final CDD inv) {
		stateInv = inv;
	}

	public CDD getClockInvariant() {
		return clockInv;
	}

	public void setClockInvariant(final CDD inv) {
		clockInv = inv;
	}

	public Set<String> getStoppedClocks() {
		return stoppedClocks;
	}

	public boolean isStopped(final String clock) {
		return stoppedClocks.contains(clock);
	}

	public List<Transition> getTransitions() {
		return transitions;
	}

	public Transition getOutgoingTransition(final Phase dest) {
		Transition result = null;

		for (final Transition transition : transitions) {
			if (transition.getDest().equals(dest)) {
				result = transition;
				break;
			}
		}

		return result;
	}

	// 处理有多个相同目的边
	public List<Transition> getOutgoingTransitions(final Phase dest) {
		ArrayList<Transition> result = new ArrayList<>();

		for (final Transition transition : transitions) {
			if (transition.getDest().equals(dest)) {
				result.add(transition);
			}
		}

		return result;
	}

	/**
	 * 合并到同一位置的变迁
	 * @param dest
	 * @param or
	 */
	public void mergeTrans(final Phase dest, boolean or) {
		List<Transition> destTrans = getOutgoingTransitions(dest);
		if (destTrans.size() > 1) {
			CDD tranGuard = CDD.TRUE;
			for (Transition tran : destTrans) {
				removeTransition(tran);
				if (or) {
					tranGuard = tranGuard.or(tran.getGuard());
				} else {
					tranGuard = tranGuard.and(tran.getGuard());
				}
			}
			addTransition(dest, tranGuard);
		}
	}

	public void updateOutgoingTransition(final Phase dest, Transition newT) {
		for (int i = 0; i < transitions.size(); i++) {
			if (transitions.get(i).getDest().equals(dest)) {
				transitions.set(i, newT);
			}
		}
	}
	public Transition addSelfTrans() {
		return addTransition(this);
	}

	public Transition addTransition(final Phase dest) {
		return addTransition(dest, CDD.TRUE, new String[]{});
	}

	public Transition addTransition(final Phase dest, final CDD guard) {
		return addTransition(dest, guard, new String[]{});
	}

	/** @return the transition added or modified */
	public Transition addTransition(final Phase dest, final CDD guard, final String[] resets) {
		final Iterator<Transition> it = transitions.iterator();

		while (it.hasNext()) {
			final Transition t = it.next();

			if ((t.getDest() == dest) && t.getResets().equals(resets)) {
				t.setGuard(t.getGuard().or(guard));

				return t;
			}
		}

		final Transition t = new Transition(this, guard, resets, dest);
		transitions.add(t);

		return t;
	}

	public Transition copyTran(Phase dest, Transition old) {
		Transition nt = addTransition(dest, old.getGuard(), old.getResets());
		// 先不考虑 clock writer 冲突的情况
		old.getClockWriter().forEach(nt::putClockWriter);
		nt.isParallel = old.isParallel;
		nt.isEventual = old.isEventual;
		nt.isError = old.isError;
		return nt;
	}

	public void removeTransition(Transition t) {
		transitions.remove(t);
	}

	public boolean containsPhantom(CDD p) {
		return phantoms.containsKey(p);
	}

	public void addPhantom(CDD p, boolean isC) {
		phantoms.put(p, isC);
	}

	public boolean hasPhantom() {
		return !phantoms.isEmpty();
	}

//	public Map<CDD, Boolean> getPhantoms() {
//		return phantoms;
//	}

	public void migratePhantom(Phase oth) {
		CDD st = oth.getStateInvariant();
		for (CDD p : phantoms.keySet()) {
			boolean c = phantoms.get(p);
			oth.addPhantom(p, c);
			st = st.andPhantom(p, c);
		}
		oth.setStateInvariant(st);
//		return oth;
	}

	public List<Phase> dePhantom() {
		List<Phase> phases = new ArrayList<>();
		if (phantoms.isEmpty()) {
			phases.add(this);
			return phases;
		}
		CDD baseStateInv = stateInv;
		CDD negPhantom = CDD.TRUE;
		List<CDD> stateInvs = new ArrayList<>();
		List<CDD> tmpStateInvs = new ArrayList<>();

		Transition selfTr = getOutgoingTransition(this);

		for (CDD cdd : phantoms.keySet()) {
			String opName = phantoms.get(cdd) ? "pc" : "p";
			baseStateInv = baseStateInv.assume(cdd.operator(opName));
		}
//		System.out.println(stateInv);
//		System.out.println(baseStateInv);
		stateInvs.add(baseStateInv);
		for (CDD cdd : phantoms.keySet()) {
			tmpStateInvs.clear();
			tmpStateInvs.addAll(stateInvs);
			for (CDD cInv : tmpStateInvs) {
				if (!cInv.isEqual(baseStateInv)) {
					stateInvs.add(cInv);
				}
				stateInvs.add(cInv.and(cdd));
			}
			negPhantom = negPhantom.and(cdd.negate());
		}
		// 先不考虑cycle
		Phase originPhase = null;
		for (int i = 0; i < stateInvs.size(); i++) {
			Phase phase = new Phase(this.name+"_"+i, stateInvs.get(i), this.clockInv, this.stoppedClocks);
			phase.isInit = this.isInit;
			for (Transition transition : transitions) {
				if (transition.getDest() == this) {
					continue;
				}
				Transition nt = phase.addTransition(transition.getDest(), transition.getGuard(), transition.getResets());
				nt.isParallel = transition.isParallel;
				nt.isEventual = transition.isEventual;
			}
			if (i>0) {
				phase.addTransition(originPhase);
			} else {
				originPhase = phase;
			}
			// 自循环
			phase.addTransition(phase, selfTr.getGuard(), selfTr.getResets());
			phases.add(phase);
		}
		if (originPhase.getStateInvariant().isEqual(CDD.TRUE)) {
			originPhase.setStateInvariant(negPhantom);
		}
		return phases;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	public void dump() {
		System.err.println("  state " + this + " { ");

		if (stateInv != CDD.TRUE) {
			System.err.println("    predicate      " + stateInv);
		}

		if (clockInv != CDD.TRUE) {
			System.err.println("    clockinvariant " + clockInv);
		}

		for (final String clock : stoppedClocks) {
			System.err.println("    stopped " + clock);
		}

		System.err.println("    transitions {");

		final Iterator<Transition> it = transitions.iterator();

		while (it.hasNext()) {
			System.err.println("       " + it.next());
		}

		System.err.println("    }");
		System.err.println("  }");
	}

	public void dumpDot() {
		System.out.println("  " + name + " [ label = \"" + stateInv + "\\n" + clockInv + "\" shape=ellipse ]");

		final Iterator<Transition> it = transitions.iterator();

		while (it.hasNext()) {
			final Transition t = it.next();
			System.out.println("  " + t.getSrc().name + " -> " + t.getDest().name + " [ label = \"" + t.getGuard() + "\" ]");
		}
	}

	public String getFlags() {
		String flags = "";

		if (isInit) {
			flags += " Init ";
		}

		if (isKernel) {
			flags += " Kernel ";
		}

		if (isEntry) {
			flags += " Entry ";
		}

		if (isExit) {
			flags += " Exit ";
		}

		return flags;
	}

	public Phase and(final Phase oth, String name) {
		Set<String> sClocks = new HashSet<String>(stoppedClocks);
		sClocks.addAll(oth.stoppedClocks);
		return new Phase(name, getStateInvariant().and(oth.getStateInvariant()),
                getClockInvariant().and(oth.getClockInvariant()), sClocks);
	}

	// jf
	public void setName(final String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Phase p) {
		return name.compareTo(p.name);
	}

	public void addIncomming(final Transition trans) {
		incomming.add(trans);
	}

	public void removeIncomming(final Transition trans) {
		incomming.remove(trans);
	}

	public void setID(final int ID) {
		this.ID = ID;
	}

	public int getID() {
		return ID;
	}

	/**
	 * test equality by state invariant
	 * @param oth
	 * @return
	 */
	public boolean equalByState(final Phase oth) {
		return getStateInvariant().and(oth.getStateInvariant()).isEqual(getStateInvariant());
	}
}
