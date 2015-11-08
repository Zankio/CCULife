package org.zankio.cculife.CCUService.base.SourceSwitcher;

import org.zankio.cculife.CCUService.base.source.ISource;

public class SingleSourceSwitcher implements ISwitcher{

    private ISource source;

    public SingleSourceSwitcher(){}

    public SingleSourceSwitcher(ISource source) {
        this.source = source;
    }

    public void setSource(ISource source) {
        this.source = source;
    }

    @Override
    public ISource getSource() {
        return source;
    }

    @Override
    public void openSource() {
        source.openSource();
    }

    @Override
    public void closeSource() {
        source.closeSource();
    }
}
