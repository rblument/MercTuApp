///*
// * MERC^T: Multiple External Representations of Computation Tutor
// *
// *  (C) Richard Blumenthal, All rights reserved
// *
// *  Unauthorized use, duplication or distribution without the authors'
// *  permission is strictly prohibited.
// *
// *  Unless required by applicable law or agreed to in writing, this
// *  software is distributed on an "AS IS" basis without warranties
// *  or conditions of any kind, either expressed or implied.
// */
//
//package edu.regis.merc.model;
//
//
//import edu.regis.merc.svc.ServiceFactory;
//import edu.regis.merc.svc.TuringMachingSvc;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.random.RandomGenerator;
//
///**
// * Test file for Turing Machine model
// *
// * @author Michael Nguyen
// */
//public class TuringMachineTest {
//    public static void main(String[] args) {
//        // Set up Input Alphabet
//        ArrayList<Character> inputAlphabet = new ArrayList<>();
//        inputAlphabet.add('0');
//        inputAlphabet.add('1');
//
//        // Set up Tape Alphabet
//        ArrayList<Character> tapeAlphabet = new ArrayList<>(inputAlphabet);
//        tapeAlphabet.add('_');
//
//        int machineIds = RandomGenerator.getDefault().nextInt();
//
//        // Declare States
//        State q0 = new State("q0", machineIds, RandomGenerator.getDefault().nextInt());
//        State qAccept = new State("qAccept", machineIds, RandomGenerator.getDefault().nextInt());
//        State qReject = new State("qReject", machineIds, RandomGenerator.getDefault().nextInt());
//
//        TuringMachine tm = new TuringMachine(machineIds, "testname", inputAlphabet, tapeAlphabet, q0, qAccept, qReject);
//        System.out.println(tm.getCurrentState());
//        TuringMachingSvc svc = ServiceFactory.findTuringMachineSvc();
//
//        q0.setMachine_id(tm.getId());
//        qAccept.setMachine_id(tm.getId());
//        qReject.setMachine_id(tm.getId());
//        q0.setMachine_id(tm.getId());
//        qAccept.setMachine_id(tm.getId());
//        qReject.setMachine_id(tm.getId());
//
//        // Add Transitions
//        Transition t0 = new Transition('0', '0', MoveKind.RIGHT, q0);
//        tm.addTransition(q0, t0);
//        Transition tBlank = new Transition('_', '_', MoveKind.RIGHT, qAccept);
//        tm.addTransition(q0, tBlank);
//
//        // Run tests
//        System.out.println("\nTuringMachine Test:");
//        System.out.println("Expected behavior: accept all strings of '0' and none of '1'\n");
//        System.out.println("Expected: Accept");
//        runTest(tm, ""); // Expected: true
//        runTest(tm, "0"); // Expected: true
//        runTest(tm, "00"); // Expected: true
//        System.out.println("\nExpected: Reject");
//        runTest(tm, "1"); // Expected: false
//        runTest(tm, "01"); // Expected: false
//        runTest(tm, "001"); // Expected: false
//
//
//        svc.createMachine(tm);
//
//        List<TuringMachine> allturing = svc.getAllMachines();
//
//        for (TuringMachine machine : allturing) {
//            // You can now access each TuringMachine object, for example, to print its name.
//            System.out.println("Processing machine: " + machine.getId());
//            System.out.println("Processing state: " + machine.getStartState());
//        }
//    }
//
//    // Test method for inputs
//    private static void runTest(TuringMachine tm, String input) {
//        tm.setInput(input);
//        tm.stepAll();
//        System.out.println("Input '" + input + "' accepted: " + tm.isAccepted());
//    }
//}
