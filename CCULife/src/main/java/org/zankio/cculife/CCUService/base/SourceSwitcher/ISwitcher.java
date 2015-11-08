package org.zankio.cculife.CCUService.base.SourceSwitcher;

import org.zankio.cculife.CCUService.base.source.ISource;

public interface ISwitcher {
    public ISource getSource();
    public void closeSource();
    public void openSource();
}
