package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    //modified processedItems and processedCount to be thread safe
    private final Queue<Item> processedItems = new ConcurrentLinkedQueue<>();
    private final AtomicInteger processedCount = new AtomicInteger(0);


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Processes a single item by its ID.
     * <p>
     * This method simulates processing delay, retrieves the item from the database,
     * updates its status to "PROCESSED", saves the updated item, and returns it.
     * Throws an exception if the item is not found or the thread is interrupted.
     *
     * @param itemId the ID of the item to process
     * @return the processed and saved {@link Item}
     * @throws RuntimeException if the item is not found or thread is interrupted
     */
    public Item processItem(Long itemId) {
        try{
            Thread.sleep(100);
            return itemRepository.findById(itemId).map(item ->
            {item.setStatus("PROCESSED");
            return itemRepository.save(item);
            }).orElseThrow(() -> new RuntimeException("Item not found"));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while processing Item " + itemId);
        }
    }

    /**
     * Asynchronously processes all items in the database.
     * <p>
     * This method retrieves all item IDs, launches a separate async task to
     * process each item, waits for all tasks to complete, then collects and
     * returns a list of all successfully processed items.
     * All shared state (such as the processed item list and count) is safely updated.
     *
     * @return a {@link CompletableFuture} containing the list of all processed {@link Item} objects
     * @throws RuntimeException if any task fails during processing
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        //launch one async task per item using supplyAsync to ensure each task is awaited and collected into list
        List<CompletableFuture<Item>> futures = itemIds.stream().map(id->
                CompletableFuture.supplyAsync(() -> processItem(id), executor)).
                toList();

        //use allOf to ensure we wait for all item-processing futures to complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v-> {
           synchronized (this) {
               futures.forEach(f->{
                   try {
                       Item item = f.join();
                       processedItems.add(item);
                       processedCount.incrementAndGet();
                   } catch (Exception e) {
                       throw new RuntimeException(e);
                   }
               });
               return List.copyOf(processedItems);
           }
        });
    }
    /**
     * There were a few problems with the original implementation that prevented the program from working correctly:
     * First, processedItems and processedCount were simple array and int variables, which are not thread safe.
     * They can be modified from multiple threads without synchronization.
     * Second, processItemsAsync used runAsync() which does launch tasks, but it does not ensure they are completed.
     * The method immediately returned processedItems, and some items may not have had time to finish processing.
     * There was also poor error handling, as the exceptions were printed and not logged properly.
     * Third was the way processItems() was set up. The return type was not ideal for async responses, as it treated it
     * like a synchronous call and did not have any logic in place to ensure the response was completed.
     * It also had no error handling.*/

}

