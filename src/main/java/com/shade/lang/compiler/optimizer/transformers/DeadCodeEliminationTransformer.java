package com.shade.lang.compiler.optimizer.transformers;

import com.shade.lang.compiler.optimizer.Transformer;
import com.shade.lang.compiler.optimizer.TransformerProvider;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.stmt.AssertStatement;
import com.shade.lang.compiler.parser.node.stmt.BlockStatement;
import com.shade.lang.compiler.parser.node.stmt.BranchStatement;
import com.shade.lang.compiler.parser.node.stmt.EmptyStatement;
import com.shade.lang.util.annotations.NotNull;

import java.util.List;

import static com.shade.lang.compiler.optimizer.TransformerUtils.asConst;
import static com.shade.lang.compiler.optimizer.TransformerUtils.isConst;

public class DeadCodeEliminationTransformer extends Transformer implements TransformerProvider {
    private static final DeadCodeEliminationTransformer INSTANCE = new DeadCodeEliminationTransformer();

    @Override
    public int getLevel() {
        return 2;
    }

    @NotNull
    @Override
    public Transformer create() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public Statement leaveAssertStatement(@NotNull AssertStatement statement) {
        if (isConst(statement.getCondition(), Boolean.class) && asConst(statement.getCondition(), Boolean.class)) {
            return EmptyStatement.INSTANCE;
        }

        return super.leaveAssertStatement(statement);
    }

    @NotNull
    @Override
    public Statement leaveBlockStatement(@NotNull BlockStatement statement) {
        final List<Statement> statements = statement.getStatements();

        for (int index = 0; index < statements.size(); index++) {
            if (statements.get(index).isControlFlowReturned()) {
                return new BlockStatement(statements.subList(0, index + 1), statement.getRegion());
            }
        }

        return super.leaveBlockStatement(statement);
    }

    @NotNull
    @Override
    public Statement leaveBranchStatement(@NotNull BranchStatement statement) {
        if (isConst(statement.getCondition(), Boolean.class)) {
            return asConst(statement.getCondition(), Boolean.class) ? statement.getPass() : statement.getFail();
        }

        return super.leaveBranchStatement(statement);
    }
}
