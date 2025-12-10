package edu.regis.merc.util;

import edu.regis.merc.model.*;

import java.util.ArrayList;

public class TuringMachineFactory {
    public static TuringMachine createTuringMachine(int id) {
        switch (id) {
            case 0:
                return createTestModel0();
            default:
                return null;
        }
    }

    // ToDo: Temporary
    public static TuringMachine createTestModel0() {
        TuringMachine tm = new TuringMachine();
        ArrayList<State> states = new ArrayList<>();

        State qStart = new State("qStart");
        tm.setStartState(qStart);
        GuiCtx ctx = createStateGuiCtx(200, 100, 30, 30);
        qStart.setGuiCtx(ctx);

        State q1 = new State("q1");
        tm.setAcceptState(q1);
        GuiCtx q1ctx = createStateGuiCtx(300, 100, 30, 30);
        q1.setGuiCtx(q1ctx);

        State qReject = new State("qReject");
        tm.setRejectState(qReject);
        GuiCtx q2ctx = createStateGuiCtx(100, 100, 30, 30);
        qReject.setGuiCtx(q2ctx);

        State qAccept = new State("qAccept");
        GuiCtx q3ctx = createStateGuiCtx(400, 100, 30, 30);
        qAccept.setGuiCtx(q3ctx);

        states.add(qStart);
        states.add(q1);
        states.add(qReject);
        states.add(qAccept);

        tm.setStartState(qStart);
        tm.setStates(states);

        Transition t0 = new Transition('_', 'X', MoveKind.RIGHT, q1);
        GuiCtx tctx0 = createTransitionGuiCtx(
                (qStart.getGuiCtx().getX() + qStart.getGuiCtx().getWidth() / 2),
                (qStart.getGuiCtx().getY() + qStart.getGuiCtx().getHeight() / 2),
                (q1.getGuiCtx().getX() + q1.getGuiCtx().getWidth() / 2),
                (q1.getGuiCtx().getY() + q1.getGuiCtx().getHeight() / 2));
        t0.setGuiCtx(tctx0);

        Transition t1 = new Transition('X', '_', MoveKind.RIGHT, qReject);
        GuiCtx tctx1 = createTransitionGuiCtx(
                (qStart.getGuiCtx().getX() + qStart.getGuiCtx().getWidth() / 2),
                (qStart.getGuiCtx().getY() + qStart.getGuiCtx().getHeight() / 2),
                (qReject.getGuiCtx().getX() + qReject.getGuiCtx().getWidth() / 2),
                (qReject.getGuiCtx().getY() + qReject.getGuiCtx().getHeight() / 2));
        t1.setGuiCtx(tctx1);

        Transition t2 = new Transition('_', '_', MoveKind.RIGHT, qAccept);
        GuiCtx tctx2 = createTransitionGuiCtx(
                (q1.getGuiCtx().getX() + q1.getGuiCtx().getWidth() / 2),
                (q1.getGuiCtx().getY() + q1.getGuiCtx().getHeight() / 2),
                (qAccept.getGuiCtx().getX() + qAccept.getGuiCtx().getWidth() / 2),
                (qAccept.getGuiCtx().getY() + qAccept.getGuiCtx().getHeight() / 2));
        t2.setGuiCtx(tctx2);

        tm.addTransition(qStart, t0);
        tm.addTransition(qStart, t1);
        tm.addTransition(qAccept, t2);

        tm.setTransitions(tm.getTransitions());
        tm.setStates(states);

        return tm;
    }

    public static GuiCtx createStateGuiCtx(int X, int Y, int width, int height) {
        GuiCtx ctx = new GuiCtx();
        ctx.setX(X);
        ctx.setY(Y);
        ctx.setWidth(width);
        ctx.setHeight(height);

        return ctx;
    }

    public static GuiCtx createTransitionGuiCtx(int X, int Y, int X2, int Y2) {
        GuiCtx ctx = new GuiCtx();
        ctx.setX(X);
        ctx.setY(Y);
        ctx.setX2(X2);
        ctx.setY2(Y2);

        return ctx;
    }
}
