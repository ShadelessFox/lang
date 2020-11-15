package com.shade.lang.optimizer.transformers;

import com.shade.lang.optimizer.SimpleTransformer;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.stmt.AssertStatement;
import com.shade.lang.parser.node.stmt.BlockStatement;
import com.shade.lang.parser.node.stmt.BranchStatement;

import java.util.List;

public class DeadCodeEliminationTransformer extends SimpleTransformer {
    @Override
    public int getLevel() {
        return 2;
    }

    @Override
    public Statement transform(AssertStatement statement) {
        AssertStatement transformed = (AssertStatement) super.transform(statement);

        if (isConst(transformed.getCondition(), Boolean.class) && (Boolean) asConst(transformed.getCondition())) {
            return null;
        }

        return transformed;
    }

    @Override
    public Statement transform(BlockStatement statement) {
        BlockStatement transformed = (BlockStatement) super.transform(statement);

        List<Statement> statements = transformed.getStatements();

        for (int index = 0; index < transformed.getStatements().size(); index++) {
            if (statements.get(index).isControlFlowReturned()) {
                statements = statements.subList(0, index + 1);
                break;
            }
        }

        if (!statements.equals(transformed.getStatements())) {
            return new BlockStatement(statements, transformed.getRegion());
        }

        return transformed;
    }

    @Override
    public Statement transform(BranchStatement statement) {
        BranchStatement transformed = (BranchStatement) super.transform(statement);

        if (isConst(transformed.getCondition(), Boolean.class)) {
            return asConst(transformed.getCondition()) ? transformed.getPass() : transformed.getFail();
        }

        return transformed;
    }
}
