package org.techmart.lk.ejb.bean;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionConsumer;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.TopicSubscriber;
import jakarta.jms.ServerSessionPool;
import jakarta.jms.CompletionListener;
import jakarta.jms.DeliveryMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryJmsProvider {

    private final Map<String, List<MessageListener>> queueListeners = new ConcurrentHashMap<>();
    private final Map<String, List<MessageListener>> topicListeners = new ConcurrentHashMap<>();

    private final List<Message> pendingMessages = new CopyOnWriteArrayList<>();

    public ConnectionFactory getConnectionFactory() {
        return new InMemoryConnectionFactory();
    }

    public jakarta.jms.Queue getQueue(String name) {
        return new InMemoryQueue(name);
    }

    public jakarta.jms.Topic getTopic(String name) {
        return new InMemoryTopic(name);
    }

    public void registerQueueListener(String name, MessageListener listener) {
        queueListeners.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void registerTopicListener(String name, MessageListener listener) {
        topicListeners.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void clearListeners() {
        queueListeners.clear();
        topicListeners.clear();
    }

    public void commitTx() {
        List<Message> messagesToDeliver = new ArrayList<>(pendingMessages);
        pendingMessages.clear();
        for (Message msg : messagesToDeliver) {
            deliverMessage(msg);
        }
    }

    public void rollbackTx() {
        pendingMessages.clear();
    }

    private void deliverMessage(Message msg) {
        try {
            Destination dest = msg.getJMSDestination();
            if (dest instanceof jakarta.jms.Queue) {
                String queueName = ((jakarta.jms.Queue) dest).getQueueName();
                List<MessageListener> listeners = queueListeners.get(queueName);
                if (listeners != null) {
                    for (MessageListener listener : listeners) {
                        CompletableFuture.runAsync(() -> listener.onMessage(msg));
                    }
                }
            } else if (dest instanceof jakarta.jms.Topic) {
                String topicName = ((jakarta.jms.Topic) dest).getTopicName();
                List<MessageListener> listeners = topicListeners.get(topicName);
                if (listeners != null) {
                    for (MessageListener listener : listeners) {
                        CompletableFuture.runAsync(() -> listener.onMessage(msg));
                    }
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private class InMemoryConnectionFactory implements ConnectionFactory {
        @Override
        public Connection createConnection() throws JMSException {
            return new InMemoryConnection();
        }

        @Override
        public Connection createConnection(String userName, String password) throws JMSException {
            return createConnection();
        }

        @Override
        public JMSContext createContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public JMSContext createContext(String userName, String password) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JMSContext createContext(String userName, String password, int sessionMode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JMSContext createContext(int sessionMode) {
            throw new UnsupportedOperationException();
        }
    }

    private class InMemoryConnection implements Connection {
        @Override
        public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
            return new InMemorySession();
        }

        @Override
        public Session createSession(int sessionMode) throws JMSException {
            return new InMemorySession();
        }

        @Override
        public Session createSession() throws JMSException {
            return new InMemorySession();
        }

        @Override
        public String getClientID() throws JMSException { return "InMemoryClient"; }

        @Override
        public void setClientID(String clientID) throws JMSException {}

        @Override
        public ConnectionMetaData getMetaData() throws JMSException { return null; }

        @Override
        public ExceptionListener getExceptionListener() throws JMSException { return null; }

        @Override
        public void setExceptionListener(ExceptionListener listener) throws JMSException {}

        @Override
        public void start() throws JMSException {}

        @Override
        public void stop() throws JMSException {}

        @Override
        public void close() throws JMSException {}

        @Override
        public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException { return null; }

        @Override
        public ConnectionConsumer createSharedConnectionConsumer(jakarta.jms.Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException { return null; }

        @Override
        public ConnectionConsumer createDurableConnectionConsumer(jakarta.jms.Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException { return null; }

        @Override
        public ConnectionConsumer createSharedDurableConnectionConsumer(jakarta.jms.Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException { return null; }
    }

    private class InMemorySession implements Session {
        @Override
        public BytesMessage createBytesMessage() throws JMSException { return null; }

        @Override
        public MapMessage createMapMessage() throws JMSException {
            return new InMemoryMapMessage();
        }

        @Override
        public Message createMessage() throws JMSException { return new InMemoryMessage(); }

        @Override
        public ObjectMessage createObjectMessage() throws JMSException { return null; }

        @Override
        public ObjectMessage createObjectMessage(Serializable object) throws JMSException { return null; }

        @Override
        public StreamMessage createStreamMessage() throws JMSException { return null; }

        @Override
        public TextMessage createTextMessage() throws JMSException { return null; }

        @Override
        public TextMessage createTextMessage(String text) throws JMSException { return null; }

        @Override
        public boolean getTransacted() throws JMSException { return false; }

        @Override
        public int getAcknowledgeMode() throws JMSException { return Session.AUTO_ACKNOWLEDGE; }

        @Override
        public void commit() throws JMSException {}

        @Override
        public void rollback() throws JMSException {}

        @Override
        public void close() throws JMSException {}

        @Override
        public void recover() throws JMSException {}

        @Override
        public MessageListener getMessageListener() throws JMSException { return null; }

        @Override
        public void setMessageListener(MessageListener listener) throws JMSException {}

        @Override
        public void run() {}

        @Override
        public MessageProducer createProducer(Destination destination) throws JMSException {
            return new InMemoryMessageProducer(destination);
        }

        @Override
        public MessageConsumer createConsumer(Destination destination) throws JMSException { return null; }

        @Override
        public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException { return null; }

        @Override
        public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean NoLocal) throws JMSException { return null; }

        @Override
        public MessageConsumer createSharedConsumer(jakarta.jms.Topic topic, String sharedSubscriptionName) throws JMSException { return null; }

        @Override
        public MessageConsumer createSharedConsumer(jakarta.jms.Topic topic, String sharedSubscriptionName, String messageSelector) throws JMSException { return null; }

        @Override
        public jakarta.jms.Queue createQueue(String queueName) throws JMSException {
            return new InMemoryQueue(queueName);
        }

        @Override
        public jakarta.jms.Topic createTopic(String topicName) throws JMSException {
            return new InMemoryTopic(topicName);
        }

        @Override
        public TopicSubscriber createDurableSubscriber(jakarta.jms.Topic topic, String name) throws JMSException { return null; }

        @Override
        public TopicSubscriber createDurableSubscriber(jakarta.jms.Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException { return null; }

        @Override
        public MessageConsumer createDurableConsumer(jakarta.jms.Topic topic, String name) throws JMSException { return null; }

        @Override
        public MessageConsumer createDurableConsumer(jakarta.jms.Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException { return null; }

        @Override
        public MessageConsumer createSharedDurableConsumer(jakarta.jms.Topic topic, String name) throws JMSException { return null; }

        @Override
        public MessageConsumer createSharedDurableConsumer(jakarta.jms.Topic topic, String name, String messageSelector) throws JMSException { return null; }

        @Override
        public QueueBrowser createBrowser(jakarta.jms.Queue queue) throws JMSException { return null; }

        @Override
        public QueueBrowser createBrowser(jakarta.jms.Queue queue, String messageSelector) throws JMSException { return null; }

        @Override
        public TemporaryQueue createTemporaryQueue() throws JMSException { return null; }

        @Override
        public TemporaryTopic createTemporaryTopic() throws JMSException { return null; }

        @Override
        public void unsubscribe(String name) throws JMSException {}
    }

    private static class InMemoryQueue implements jakarta.jms.Queue {
        private final String name;
        public InMemoryQueue(String name) { this.name = name; }
        @Override
        public String getQueueName() throws JMSException { return name; }
        @Override
        public String toString() { return name; }
    }

    private static class InMemoryTopic implements jakarta.jms.Topic {
        private final String name;
        public InMemoryTopic(String name) { this.name = name; }
        @Override
        public String getTopicName() throws JMSException { return name; }
        @Override
        public String toString() { return name; }
    }

    private class InMemoryMessageProducer implements MessageProducer {
        private final Destination destination;
        public InMemoryMessageProducer(Destination destination) { this.destination = destination; }

        @Override
        public void setDisableMessageID(boolean value) throws JMSException {}
        @Override
        public boolean getDisableMessageID() throws JMSException { return false; }
        @Override
        public void setDisableMessageTimestamp(boolean value) throws JMSException {}
        @Override
        public boolean getDisableMessageTimestamp() throws JMSException { return false; }
        @Override
        public void setDeliveryMode(int deliveryMode) throws JMSException {}
        @Override
        public int getDeliveryMode() throws JMSException { return DeliveryMode.PERSISTENT; }
        @Override
        public void setPriority(int defaultPriority) throws JMSException {}
        @Override
        public int getPriority() throws JMSException { return 4; }
        @Override
        public void setTimeToLive(long timeToLive) throws JMSException {}
        @Override
        public long getTimeToLive() throws JMSException { return 0; }
        @Override
        public void setDeliveryDelay(long deliveryDelay) throws JMSException {}
        @Override
        public long getDeliveryDelay() throws JMSException { return 0; }
        @Override
        public Destination getDestination() throws JMSException { return destination; }
        @Override
        public void close() throws JMSException {}

        @Override
        public void send(Message message) throws JMSException {
            message.setJMSDestination(destination);
            pendingMessages.add(message);
        }

        @Override
        public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
            send(message);
        }

        @Override
        public void send(Destination destination, Message message) throws JMSException {
            message.setJMSDestination(destination);
            pendingMessages.add(message);
        }

        @Override
        public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
            send(destination, message);
        }

        @Override
        public void send(Message message, CompletionListener completionListener) throws JMSException {}

        @Override
        public void send(Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {}

        @Override
        public void send(Destination destination, Message message, CompletionListener completionListener) throws JMSException {}

        @Override
        public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {}
    }

    private static class InMemoryMessage implements Message {
        protected Destination destination;
        protected final Map<String, Object> properties = new HashMap<>();

        @Override
        public String getJMSMessageID() throws JMSException { return "ID:" + UUID.randomUUID().toString(); }
        @Override
        public void setJMSMessageID(String id) throws JMSException {}
        @Override
        public long getJMSTimestamp() throws JMSException { return System.currentTimeMillis(); }
        @Override
        public void setJMSTimestamp(long timestamp) throws JMSException {}
        @Override
        public byte[] getJMSCorrelationIDAsBytes() throws JMSException { return null; }
        @Override
        public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {}
        @Override
        public void setJMSCorrelationID(String correlationID) throws JMSException {}
        @Override
        public String getJMSCorrelationID() throws JMSException { return null; }
        @Override
        public Destination getJMSReplyTo() throws JMSException { return null; }
        @Override
        public void setJMSReplyTo(Destination replyTo) throws JMSException {}
        @Override
        public Destination getJMSDestination() throws JMSException { return destination; }
        @Override
        public void setJMSDestination(Destination destination) throws JMSException { this.destination = destination; }
        @Override
        public int getJMSDeliveryMode() throws JMSException { return DeliveryMode.PERSISTENT; }
        @Override
        public void setJMSDeliveryMode(int deliveryMode) throws JMSException {}
        @Override
        public boolean getJMSRedelivered() throws JMSException { return false; }
        @Override
        public void setJMSRedelivered(boolean redelivered) throws JMSException {}
        @Override
        public String getJMSType() throws JMSException { return null; }
        @Override
        public void setJMSType(String type) throws JMSException {}
        @Override
        public long getJMSExpiration() throws JMSException { return 0; }
        @Override
        public void setJMSExpiration(long expiration) throws JMSException {}
        @Override
        public long getJMSDeliveryTime() throws JMSException { return 0; }
        @Override
        public void setJMSDeliveryTime(long deliveryTime) throws JMSException {}
        @Override
        public int getJMSPriority() throws JMSException { return 4; }
        @Override
        public void setJMSPriority(int priority) throws JMSException {}
        @Override
        public void clearProperties() throws JMSException { properties.clear(); }
        @Override
        public boolean propertyExists(String name) throws JMSException { return properties.containsKey(name); }
        @Override
        public boolean getBooleanProperty(String name) throws JMSException { return (Boolean) properties.get(name); }
        @Override
        public byte getByteProperty(String name) throws JMSException { return (Byte) properties.get(name); }
        @Override
        public short getShortProperty(String name) throws JMSException { return (Short) properties.get(name); }
        @Override
        public int getIntProperty(String name) throws JMSException { return (Integer) properties.get(name); }
        @Override
        public long getLongProperty(String name) throws JMSException { return (Long) properties.get(name); }
        @Override
        public float getFloatProperty(String name) throws JMSException { return (Float) properties.get(name); }
        @Override
        public double getDoubleProperty(String name) throws JMSException { return (Double) properties.get(name); }
        @Override
        public String getStringProperty(String name) throws JMSException { return (String) properties.get(name); }
        @Override
        public Object getObjectProperty(String name) throws JMSException { return properties.get(name); }
        @Override
        public Enumeration getPropertyNames() throws JMSException { return Collections.enumeration(properties.keySet()); }
        @Override
        public void setBooleanProperty(String name, boolean value) throws JMSException { properties.put(name, value); }
        @Override
        public void setByteProperty(String name, byte value) throws JMSException { properties.put(name, value); }
        @Override
        public void setShortProperty(String name, short value) throws JMSException { properties.put(name, value); }
        @Override
        public void setIntProperty(String name, int value) throws JMSException { properties.put(name, value); }
        @Override
        public void setLongProperty(String name, long value) throws JMSException { properties.put(name, value); }
        @Override
        public void setFloatProperty(String name, float value) throws JMSException { properties.put(name, value); }
        @Override
        public void setDoubleProperty(String name, double value) throws JMSException { properties.put(name, value); }
        @Override
        public void setStringProperty(String name, String value) throws JMSException { properties.put(name, value); }
        @Override
        public void setObjectProperty(String name, java.lang.Object value) throws JMSException { properties.put(name, value); }
        @Override
        public void acknowledge() throws JMSException {}
        @Override
        public void clearBody() throws JMSException {}
        @Override
        public <T> T getBody(Class<T> c) throws JMSException { return null; }
        @Override
        public boolean isBodyAssignableTo(Class c) throws JMSException { return false; }
    }

    private static class InMemoryMapMessage extends InMemoryMessage implements MapMessage {
        private final Map<String, Object> body = new HashMap<>();

        @Override
        public boolean getBoolean(String name) throws JMSException { return (Boolean) body.get(name); }
        @Override
        public byte getByte(String name) throws JMSException { return (Byte) body.get(name); }
        @Override
        public short getShort(String name) throws JMSException { return (Short) body.get(name); }
        @Override
        public char getChar(String name) throws JMSException { return (Character) body.get(name); }
        @Override
        public int getInt(String name) throws JMSException { return (Integer) body.get(name); }
        @Override
        public long getLong(String name) throws JMSException { return (Long) body.get(name); }
        @Override
        public float getFloat(String name) throws JMSException { return (Float) body.get(name); }
        @Override
        public double getDouble(String name) throws JMSException { return (Double) body.get(name); }
        @Override
        public String getString(String name) throws JMSException { return (String) body.get(name); }
        @Override
        public byte[] getBytes(String name) throws JMSException { return (byte[]) body.get(name); }
        @Override
        public Object getObject(String name) throws JMSException { return body.get(name); }
        @Override
        public Enumeration getMapNames() throws JMSException { return Collections.enumeration(body.keySet()); }
        @Override
        public void setBoolean(String name, boolean value) throws JMSException { body.put(name, value); }
        @Override
        public void setByte(String name, byte value) throws JMSException { body.put(name, value); }
        @Override
        public void setShort(String name, short value) throws JMSException { body.put(name, value); }
        @Override
        public void setChar(String name, char value) throws JMSException { body.put(name, value); }
        @Override
        public void setInt(String name, int value) throws JMSException { body.put(name, value); }
        @Override
        public void setLong(String name, long value) throws JMSException { body.put(name, value); }
        @Override
        public void setFloat(String name, float value) throws JMSException { body.put(name, value); }
        @Override
        public void setDouble(String name, double value) throws JMSException { body.put(name, value); }
        @Override
        public void setString(String name, String value) throws JMSException { body.put(name, value); }
        @Override
        public void setBytes(String name, byte[] value) throws JMSException { body.put(name, value); }
        @Override
        public void setBytes(String name, byte[] value, int offset, int length) throws JMSException { body.put(name, value); }
        @Override
        public void setObject(String name, Object value) throws JMSException { body.put(name, value); }
        @Override
        public boolean itemExists(String name) throws JMSException { return body.containsKey(name); }
    }
}
