package ca.media.mp3.entity;

import java.util.ArrayList;
import java.util.List;

public class Frame 
{
  public static final int HEADER_SIZE = 10;  
  private final String content;
  private final Header header;

  public Frame(
    int[] data)
  {
    checkIfDataIsNull(data);
    checkIfDataIsTooShort(data);
    checkIfSizeDescriptorIsValid(data);
    checkIfEncodingIsValid(data);

    header = new Header(data);
    content = getContent(data);
  }

  private String getContent(
    final int[] data) 
  {
    List<Character> characterList = getContentAsACharacterList(data);
    return characterListToString(characterList);
  }

  private String characterListToString(
    List<Character> characterList) 
  {
    return 
      characterList.
      stream().
      map(c -> c.toString()).
      reduce((s1, s2) -> s1.concat(s2)).
      orElse("");
  }

  private List<Character> getContentAsACharacterList(
    final int[] data) 
  {
    List<Character> contentList = new ArrayList<>();
    for(int i = HEADER_SIZE + 1; i < data.length; i++) 
    {
      contentList.add(new Character((char)data[i]));
    }
    return contentList;
  }

  private static void checkIfEncodingIsValid(
    final int[] data) 
  {
    if (data.length > HEADER_SIZE && data[HEADER_SIZE] != 0x00) 
    {
      throw new IllegalArgumentException("Encoding byte is invalid");
    }
  }

  private static void checkIfDataIsTooShort(
    final int[] data) 
  {
    if(data.length < HEADER_SIZE) 
    {
      throw new IllegalArgumentException(
        "Length of the data array passed as a parameter is less than the valid size for a frame's header");
    }
  }

  private static void checkIfDataIsNull(
    final int[] data) 
  {
    if(data == null) 
    {
      throw new IllegalArgumentException("Data array passed as a parameter is null");
    }
  }

  private static void checkIfSizeDescriptorIsValid(
    final int[] data) 
  {
    if(!Header.isSizeDescriptorValid(data)) 
    {
      throw new IllegalArgumentException("One or more of the four size bytes is more or equal to " + Header.MAXIMUM_SIZE_VALUE);
    }
  }

  public static boolean isValid(
    final int[] data) 
  {
    checkIfDataIsNull(data);
    checkIfDataIsTooShort(data);
    checkIfEncodingIsValid(data);
    checkIfSizeDescriptorIsValid(data);
    return true;
  }

  public static int calculateContentSize(
    final int[] data) 
  {
    checkIfDataIsNull(data);
    checkIfDataIsTooShort(data);
    checkIfSizeDescriptorIsValid(data); 
    return Header.calculateContentSize(data);
  }

  public String toString() 
  {
    return String.format("{%s, \"content\":\"%s\"}", header.toString(), content);
  }
  
  public String getContent() {
  	return content;
  }
  
  public Header getHeader() {
  	return header;
  }

  private static class Header 
  {
    public static final int MAXIMUM_SIZE_VALUE = 128;
    private final String id;
    private final int contentSize;
    private final int firstFlag;
    private final int secondFlag;
    
    public Header(
      int[] data) 
    {
      id = String.format("%c%c%c%c", data[0], data[1], data[2], data[3]);
      contentSize = calculateContentSize(data);
      firstFlag = data[8];
      secondFlag = data[9];
    }
    
    public static boolean isSizeDescriptorValid(
      final int[] data) 
    {
      return 
        data[4] < MAXIMUM_SIZE_VALUE && 
        data[5] < MAXIMUM_SIZE_VALUE && 
        data[6] < MAXIMUM_SIZE_VALUE && 
        data[7] < MAXIMUM_SIZE_VALUE;
    }
    
    public static int calculateContentSize(
      final int[] data) 
    {
      return (int)(
        data[4] * Math.pow(MAXIMUM_SIZE_VALUE, 3) +
        data[5] * Math.pow(MAXIMUM_SIZE_VALUE, 2) +
        data[6] * MAXIMUM_SIZE_VALUE +
        data[7]);
    }
    
    public String getId() {
    	return id;
    }
    
    public int getContentSize() {
    	return contentSize;
    }
    
    public int firstFlag() {
    	return firstFlag;
    }
    
    public int secondFlag() {
    	return secondFlag;
    }
    
    public String toString()
    {
      String format = "\"id\":\"%s\", \"size\":%d, \"flags\":{\"first\":%d, \"second\":%d}";
      return String.format(format, id, contentSize, firstFlag, secondFlag);
    }
  }
}
