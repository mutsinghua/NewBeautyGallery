package org.androidbook.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FileUtils
{

	/**
	 * 复制文件
	 * 
	 * @param targetDir
	 *            目标目录
	 * @param source
	 *            要复制的文件列表
	 */
	public static void copyFiles(String targetDir, ArrayList<File> source)
	{
		if (targetDir == null || source == null)
		{
			return;
		}

		for (int i = 0; i < source.size(); i++)
		{
			File fileToCopy = source.get(i);

			BufferedInputStream dis = null;
			InputStream is = null;
			BufferedOutputStream dos = null;
			String path = targetDir + File.separatorChar + fileToCopy.getName();
			try
			{
				is = new FileInputStream(fileToCopy);

				dis = new BufferedInputStream(is);

				File file = new File(path);
				if (file.exists())
				{
					file.delete();
				}
				dos = new BufferedOutputStream(new FileOutputStream(path));
				doCopy(dis, dos);

			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally
			{
				if (dis != null)
				{
					try
					{
						dis.close();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (dos != null)
				{
					try
					{
						dos.close();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}
		return;
	}

	/**
	 * 复制单个文件
	 * @param sourcePath 原路径
	 * @param targetPath 目标路径
	 */
	public static void copyFile(String sourcePath, String targetPath)
	{
		File source = new File(sourcePath);
		if( !source.exists())
		{
			return;
		}
		File target = new File(targetPath);
		if( target.exists())
		{
			target.delete();
		}
		BufferedOutputStream bufferedOutputStream = null;
		BufferedInputStream bufferedInputStream = null;
		try
		{
			bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(target));
			bufferedInputStream = new BufferedInputStream(new FileInputStream(source));
			
			doCopy(bufferedInputStream, bufferedOutputStream);
			
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 finally
			{
				if (bufferedInputStream != null)
				{
					try
					{
						bufferedInputStream.close();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (bufferedOutputStream != null)
				{
					try
					{
						bufferedOutputStream.close();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
	}
	
	/**
	 * 复制一个文件
	 */
	private static void doCopy(BufferedInputStream dis, BufferedOutputStream dos)
	{
		byte[] buf = new byte[64 * 1024];
		try
		{
			int r = dis.read(buf);
			while (r != -1)
			{
				dos.write(buf, 0, r);
				r = dis.read(buf);

			}

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 清空目录及其下面的内容
	 * 
	 * @param targetDir
	 */
	public static void clearDir(String targetDir)
	{
//		System.out.println("清空目录："+targetDir);
		traverseDir(targetDir,null,new IOnGetFileHandler()
		{
			
			@Override
			public void handle(File file)
			{
				file.delete();
			}
		},null, new IOnGetFileHandler()
		{
			
			@Override
			public void handle(File file)
			{
				file.delete();
				
			}
		});
		
		
	}

	

	/**
	 * 遍历目录树
	 * 
	 * @param dirFullPath
	 *            目录路径
	 * @param filter
	 *            文件过渡器
	 * @param handler
	 *            文件过理函数
	 */
	public static void traverseDir(String dirFullPath, FilenameFilter filter, IOnGetFileHandler handler)
	{
		File file = new File(dirFullPath);
		
		if(!file.exists())
		{
//			System.out.println("// "+ dir + " does not exits");
			return;
		}
		
		if( file.isFile())
		{
			handler.handle(file);
			return;
		}
		
		if( file.isDirectory())
		{
			File[] doc = file.listFiles(filter);
			for( File f:doc)
			{
				traverseDir(f.getAbsolutePath(), filter,handler);
			}
		}
	}

	/**
	 * 遍历目录树
	 * 
	 * @param dirFullPath
	 *            目录路径
	 * @param filter
	 *            文件过渡器
	 * @param handler
	 *            文件过理函数
	 * @param firstHandler
	 *            前序遍历处理
	 * @param lastHandler
	 *            后序遍历处理
	 */
	public static void traverseDir(String dirFullPath, FilenameFilter filter, IOnGetFileHandler handler, IOnGetFileHandler firstHandler, IOnGetFileHandler lastHandler)
	{
		File file = new File(dirFullPath);
		
		if(!file.exists())
		{
//			System.out.println("// "+ dir + " does not exits");
			return;
		}
		
		if( file.isFile())
		{
			if( handler != null)
			{
				handler.handle(file);
			}
			return;
		}
		
		if( file.isDirectory())
		{
			if( firstHandler != null)
			{
				firstHandler.handle(file);
			}
			File[] doc = file.listFiles(filter);
			for( File f:doc)
			{
				traverseDir(f.getAbsolutePath(), filter,handler);
			}
			if( lastHandler != null)
			{
				lastHandler.handle(file);
			}
		}
	}
	
	
	
	
	public static void copyFolder(final String sourcePath, final String targetPath)
	{
//		System.out.println("copyFolder from " );
//		System.out.println( sourcePath);
//		System.out.println(targetPath);
//		System.out.println("=================================");
		File tfile = new File(targetPath);
		tfile.mkdirs();
		File sfile = new File(sourcePath);
		final FilenameFilter ffilter = new FilenameFilter()
		{
			
			@Override
			public boolean accept(File dir, String name)
			{
				if( name.startsWith(".svn"))
				{
					return false;
				}
				return true;
			}
		};
		File[] files = sfile.listFiles(ffilter);
		
		ArrayList <File> cpFile = new ArrayList<File>();
		for( int i=0;i<files.length;i++)
		{
			final File souFile = files[i];
			if( souFile.isDirectory())
			{
				final String souPath = souFile.getAbsolutePath();
				final String tarpath = targetPath + File.separator + souFile.getName();
				traverseDir(souPath, ffilter, new IOnGetFileHandler()
				{
					
					@Override
					public void handle(File file)
					{
//						copyFile(file.getAbsolutePath(), tarpath +File.separator + file.getName());
						
					}
				}, new IOnGetFileHandler()
				{
					
					@Override
					public void handle(File file)
					{
						copyFolder(file.getAbsolutePath(), tarpath);
						
					}
				}, null);
			}
			else
			{
				cpFile.add(souFile);
			}
			
		}
		
		copyFiles(targetPath, cpFile);
		
		
	}
}
