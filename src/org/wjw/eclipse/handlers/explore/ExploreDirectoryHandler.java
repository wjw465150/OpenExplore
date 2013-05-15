package org.wjw.eclipse.handlers.explore;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.wjw.openexplore.Activator;

public class ExploreDirectoryHandler extends AbstractHandler {
	public static final String DEFAULT_EXPLORER_PROGRAM_PATH = "explorer";
	public static final String EXPLORER_PROGRAM_PATH_KEY = "explorer.program.path";

	/**
	 * The constructor.
	 */
	public ExploreDirectoryHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		File folderPath = getFolderPath(selection);
		if (folderPath != null) {
			try {
				if (folderPath.isFile()) {
					Runtime.getRuntime().exec(new String[] { getExplorerProgramPath(), "/select,", folderPath.getAbsolutePath() });
				} else {
					Runtime.getRuntime().exec(new String[] { getExplorerProgramPath(), folderPath.getAbsolutePath() });
				}
			} catch (IOException e) {
				logError("Failed to explore " + folderPath, e);
			}
		}
		return null;
	}

	private File getFolderPath(final ISelection selection) {
		if (selection == null) {
			return getWorkspaceFolderPath();
		}

		if (!(selection instanceof IStructuredSelection)) {
			return getWorkspaceFolderPath();
		}
		IStructuredSelection sel = (IStructuredSelection) selection;
		if (sel.isEmpty()) {
			return getWorkspaceFolderPath();
		}
		Object obj = sel.getFirstElement();

		if ((obj instanceof IPackageFragmentRoot)) {
			try {
				return ((IPackageFragmentRoot) obj).getPath().toFile();
			} catch (Exception e) {
				logError("Failed to explore " + ((IJavaElement) obj).getElementName(), e);
				return null;
			}

		}

		if ((!(obj instanceof IResource)) && ((obj instanceof IAdaptable))) {
			IResource res = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
			if (res != null) {
				obj = res;
			}
		}

		if ((obj instanceof IFile)) {
			return ((IFile) obj).getLocation().toFile();
		}

		if ((obj instanceof IContainer)) {
			return ((IContainer) obj).getLocation().toFile();
		}

		return getWorkspaceFolderPath();
	}

	private File getWorkspaceFolderPath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
	}

	public static String getExplorerProgramPath() {
		return getProgramPreferenceStore().getString(EXPLORER_PROGRAM_PATH_KEY);
	}

	public static void setExplorerProgramPath(String path) {
		String value = path;
		if (value == null)
			value = DEFAULT_EXPLORER_PROGRAM_PATH;
		value = value.trim();
		if (value.length() == 0)
			value = DEFAULT_EXPLORER_PROGRAM_PATH;
		getProgramPreferenceStore().setValue(EXPLORER_PROGRAM_PATH_KEY, value);
	}

	private static IPreferenceStore getProgramPreferenceStore() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(EXPLORER_PROGRAM_PATH_KEY, DEFAULT_EXPLORER_PROGRAM_PATH);
		return store;
	}

	public static void logError(Throwable exception) {
		logError("Unexpected Exception", exception);
	}

	public static void logError(String message, Throwable exception) {
		Status status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), IStatus.OK, message, exception);
		Activator.getDefault().getLog().log(status);
	}

}
