package edu.wpi.first.wpilibj.system;

import java.util.function.BiFunction;
import java.util.function.Function;

import edu.wpi.first.wpiutil.math.Matrix;
import edu.wpi.first.wpiutil.math.MatrixUtils;
import edu.wpi.first.wpiutil.math.Nat;
import edu.wpi.first.wpiutil.math.Num;
import edu.wpi.first.wpiutil.math.numbers.N1;

public class NumericalJacobian {
    private static final double kEpsilon = 1e-5;

    /**
     * Computes the numerical Jacobian with respect to x for f(x).
     * 
     * @param rows Number of rows in the result of f(x).
     * @param cols Number of columns in the result of f(x).
     * @param f    Vector-valued function from which to compute the Jacobian.
     * @param x    Vector argument.
     */
    public static <Rows extends Num, Cols extends Num, States extends Num> Matrix<Rows, Cols> numericalJacobian(
        Nat<Rows> rows,
        Nat<Cols> cols,
        Function<Matrix<Cols, N1>, Matrix<States, N1>> f,
        Matrix<Cols, N1> x
    ) {
        var result = MatrixUtils.zeros(rows, cols);

        for (int i = 0; i < cols.getNum(); i++) {
            var dxPlus = x.copy();
            var dxMinus = x.copy();
            dxPlus.set(i, 0, dxPlus.get(i, 0) + kEpsilon);
            dxMinus.set(i, 0, dxMinus.get(i, 0) - kEpsilon);
            var dF = f.apply(dxPlus).minus(f.apply(dxMinus)).div(2 * kEpsilon);

            result.getStorage().setColumn(i, 0, dF.getStorage().getDDRM().getData());
        }

        return result;
    }

    /**
     * Returns numerical Jacobian with respect to x for f(x, u, ...).
     * 
     * @param rows   Number of rows in the result of f(x, u).
     * @param states Number of rows in x.
     * @param f      Vector-valued function from which to compute Jacobian.
     * @param x      State vector.
     * @param u      Input vector.
     * @return
     */
    public static <Rows extends Num, States extends Num, Inputs extends Num, D extends Num> Matrix<Rows, States> numericalJacobianX(
        Nat<Rows> rows,
        Nat<States> states,
        BiFunction<Matrix<States, N1>, Matrix<Inputs, N1>, Matrix<D, N1>> f,
        Matrix<States, N1> x,
        Matrix<Inputs, N1> u
    ) {
        return numericalJacobian(rows, states, (_x) -> f.apply(_x, u), x);
    }

    /**
     * Returns numerical Jacobian with respect to u for f(x, u).
     * 
     * @param rows   Number of rows in the result of f(x, u).
     * @param inputs Number of rows in u.
     * @param f      Vector-valued function from which to compute the Jacobian.
     * @param x      State vector.
     * @param u      Input vector.
     */
    public static <Rows extends Num, States extends Num, Inputs extends Num> Matrix<Rows, Inputs> numericalJacobianU(
        Nat<Rows> rows,
        Nat<Inputs> inputs,
        BiFunction<Matrix<States, N1>, Matrix<Inputs, N1>, Matrix<States, N1>> f,
        Matrix<States, N1> x,
        Matrix<Inputs, N1> u
    ) {
        return numericalJacobian(rows, inputs, (_u) -> f.apply(x, _u), u);
    }
}
