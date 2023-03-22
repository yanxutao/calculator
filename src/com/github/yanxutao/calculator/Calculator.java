package com.github.yanxutao.calculator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Scanner;

/*
Calculator 计算器类
实现了整数的加减乘除
支持undo和redo
支持运算优先级
 */
public class Calculator {

    private static final char COMPUTE = '=';
    private static final char UNDO = 'u';
    private static final char REDO = 'r';

    private static final char ADD = '+';
    private static final char MINUS = '-';
    private static final char MULTIPLY = '*';
    private static final char DIVIDE = '/';

    private static final HashSet<Character> validOps = new HashSet<>();
    static {
        validOps.add(ADD);
        validOps.add(MINUS);
        validOps.add(MULTIPLY);
        validOps.add(DIVIDE);
    }

    // 上一次计算/compute的结果
    private int lastResult;

    // 用户输入的计算表达式
    private final StringBuilder expr = new StringBuilder();

    // 操作符栈
    private final Deque<Character> opStack = new ArrayDeque<>();
    // 操作数栈
    private final Deque<Integer> opNumStack = new ArrayDeque<>();

    // 用于undo和redo
    private final Deque<Character> redoStack = new ArrayDeque<>();

    private void process(char ch) throws Exception {
        switch (ch) {
            case UNDO:
                undo();
                break;
            case REDO:
                redo();
                break;
            case COMPUTE:
                compute();
                break;
            default:
                if (isValid(ch)) {
                    expr.append(ch);
                } else {
                    throw new Exception("非法字符：" + ch);
                }
        }
    }

    public void undo() {
        int n = expr.length();
        if (n == 0) {
            return;
        }

        redoStack.push(expr.charAt(n - 1));
        expr.deleteCharAt(n - 1);
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }

        expr.append(redoStack.pop());
    }

    public void compute() throws Exception {
        int n = expr.length();
        if (n == 0) {
            return;
        }

        // 如果表达式的首字符为操作符，则用上一次计算的结果作为第一个操作数
        int firstIdx = 0;
        char firstChar = expr.charAt(firstIdx);
        if (isOp(firstChar)) {
            opStack.push(firstChar);
            opNumStack.push(lastResult);
            firstIdx++;
        }

        // 如果表达式的末字符为操作符，则忽略
        int lastIdx = n - 1;
        char lastChar = expr.charAt(lastIdx);
        if (isOp(lastChar)) {
             lastIdx--;
        }

        int opNum = 0;
        for (int i = firstIdx; i <= lastIdx; i++) {
            char ch = expr.charAt(i);

            // 当前字符为操作数
            if (Character.isDigit(ch)) {
                opNum = opNum * 10 + (ch - '0');
                continue;
            }

            // 当前字符为操作符
            opNumStack.push(opNum);
            opNum = 0;
            if (!opStack.isEmpty()
                    && (opStack.peek() == MULTIPLY || opStack.peek() == DIVIDE)) {
                calcTwoNum();
            }
            opStack.push(ch);
        }
        opNumStack.push(opNum);

        while (!opStack.isEmpty()) {
            calcTwoNum();
        }

        int result = opNumStack.pop();
        System.out.println(result);
        reset();
        lastResult = result;
    }

    private void calcTwoNum() throws Exception {
        char op = opStack.pop();
        int num2 = opNumStack.pop();
        int num1 = opNumStack.pop();
        switch (op) {
            case ADD:
                opNumStack.push(num1 + num2);
                break;
            case MINUS:
                opNumStack.push(num1 - num2);
                break;
            case MULTIPLY:
                opNumStack.push(num1 * num2);
                break;
            case DIVIDE:
                if (num2 == 0) {
                    throw new Exception("除数不能为0");
                }
                opNumStack.push(num1 / num2);
                break;
            default:
        }
    }

    private void resolveException(Exception e) {
        System.out.println(e.getMessage());
        reset();
    }

    private void reset() {
        lastResult = 0;

        expr.delete(0, expr.length());
        opStack.clear();
        opNumStack.clear();
        redoStack.clear();
    }

    private boolean isValid(char ch) {
        if (Character.isDigit(ch)) {
            return true;
        }

        if (isOp(ch)) {
            // 不允许出现连续两个字符都是操作符，如++
            int n = expr.length();
            return n <= 0 || !isOp(expr.charAt(n - 1));
        }

        return false;
    }

    private boolean isOp(char ch) {
        return validOps.contains(ch);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Calculator calculator = new Calculator();
        while (sc.hasNext()) {
            String line = sc.nextLine();
            try {
                for (int i = 0; i < line.length(); i++) {
                    calculator.process(line.charAt(i));
                }
                // 换行触发计算
                calculator.compute();
            } catch (Exception e) {
                calculator.resolveException(e);
            }
        }
    }

}
